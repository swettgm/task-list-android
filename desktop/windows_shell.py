"""Windows taskbar identity shared by the native window and installed shortcuts."""
import ctypes
import sys
import uuid
from pathlib import Path

APP_USER_MODEL_ID = 'Personal.Tasks.PC'


class GUID(ctypes.Structure):
    _fields_ = [('bytes', ctypes.c_ubyte * 16)]
    def __init__(self, value):
        super().__init__();self.bytes[:] = uuid.UUID(value).bytes_le


class PROPERTYKEY(ctypes.Structure):
    _fields_ = [('fmtid', GUID), ('pid', ctypes.c_uint32)]


class BLOB(ctypes.Structure):
    _fields_ = [('size', ctypes.c_uint32), ('pointer', ctypes.c_void_p)]


class VALUE(ctypes.Union):
    _fields_ = [('pointer', ctypes.c_void_p), ('integer', ctypes.c_int64), ('boolean', ctypes.c_int16), ('blob', BLOB)]


class PROPVARIANT(ctypes.Structure):
    _fields_ = [('vt', ctypes.c_uint16), ('reserved', ctypes.c_uint16 * 3), ('value', VALUE)]


def check(result):
    if result < 0:raise OSError('Windows taskbar property failed: 0x%08x' % (result & 0xffffffff))


def set_process_identity():
    shell=ctypes.WinDLL('shell32');shell.SetCurrentProcessExplicitAppUserModelID.argtypes=[ctypes.c_wchar_p];shell.SetCurrentProcessExplicitAppUserModelID.restype=ctypes.c_int32
    check(shell.SetCurrentProcessExplicitAppUserModelID(APP_USER_MODEL_ID))


def window_properties(hwnd, values=None, read=()):
    ole=ctypes.WinDLL('ole32');ole.CoInitialize.argtypes=[ctypes.c_void_p];ole.CoInitialize.restype=ctypes.c_int32
    initialized=ole.CoInitialize(None)>=0
    shell=ctypes.WinDLL('shell32');shell.SHGetPropertyStoreForWindow.argtypes=[ctypes.c_void_p,ctypes.POINTER(GUID),ctypes.POINTER(ctypes.c_void_p)];shell.SHGetPropertyStoreForWindow.restype=ctypes.c_int32
    store=ctypes.c_void_p()
    try:
        check(shell.SHGetPropertyStoreForWindow(hwnd,ctypes.byref(GUID('886d8eeb-8cf2-4446-8d02-cdba1dbdcf99')),ctypes.byref(store)))
        table=ctypes.cast(store,ctypes.POINTER(ctypes.POINTER(ctypes.c_void_p))).contents
        get=ctypes.WINFUNCTYPE(ctypes.c_int32,ctypes.c_void_p,ctypes.POINTER(PROPERTYKEY),ctypes.POINTER(PROPVARIANT))(table[5])
        put=ctypes.WINFUNCTYPE(ctypes.c_int32,ctypes.c_void_p,ctypes.POINTER(PROPERTYKEY),ctypes.POINTER(PROPVARIANT))(table[6])
        commit=ctypes.WINFUNCTYPE(ctypes.c_int32,ctypes.c_void_p)(table[7])
        key=lambda pid:PROPERTYKEY(GUID('9f4c2855-9f79-4b39-a8d0-e1d42de1d5f3'),pid)
        for pid,value in (values or {}).items():
            prop=PROPVARIANT();buffer=None
            if isinstance(value,bool):prop.vt=11;prop.value.boolean=-1 if value else 0
            else:buffer=ctypes.create_unicode_buffer(value);prop.vt=31;prop.value.pointer=ctypes.cast(buffer,ctypes.c_void_p).value
            check(put(store,ctypes.byref(key(pid)),ctypes.byref(prop)))
        if values:check(commit(store))
        result={}
        for pid in read:
            prop=PROPVARIANT();check(get(store,ctypes.byref(key(pid)),ctypes.byref(prop)))
            try:result[pid]=ctypes.wstring_at(prop.value.pointer) if prop.vt==31 and prop.value.pointer else bool(prop.value.boolean) if prop.vt==11 else None
            finally:ole.PropVariantClear(ctypes.byref(prop))
        return result
    finally:
        if store:
            table=ctypes.cast(store,ctypes.POINTER(ctypes.POINTER(ctypes.c_void_p))).contents
            ctypes.WINFUNCTYPE(ctypes.c_uint32,ctypes.c_void_p)(table[2])(store)
        if initialized:ole.CoUninitialize()


def identify_window(hwnd):
    executable=str(Path(sys.executable).resolve())
    command='"'+executable+'"'
    if not getattr(sys,'frozen',False):command+=' "'+str(Path(__file__).with_name('tasks_pc.py'))+'"'
    window_properties(hwnd,{5:APP_USER_MODEL_ID,2:command,3:executable+',0',4:'Tasks',9:False})
