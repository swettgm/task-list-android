"""Exercise the real packaged Tasks window and its Windows taskbar identity."""
import argparse
import ctypes
import json
import os
from pathlib import Path
import subprocess
import tempfile
import time
import urllib.request
from windows_shell import APP_USER_MODEL_ID,window_properties

user=ctypes.WinDLL('user32');kernel=ctypes.WinDLL('kernel32')
user.GetWindowTextLengthW.argtypes=[ctypes.c_void_p];user.GetWindowTextW.argtypes=[ctypes.c_void_p,ctypes.c_wchar_p,ctypes.c_int]
user.GetWindowThreadProcessId.argtypes=[ctypes.c_void_p,ctypes.POINTER(ctypes.c_uint32)]
user.IsWindowVisible.argtypes=[ctypes.c_void_p];user.IsIconic.argtypes=[ctypes.c_void_p]
user.PostMessageW.argtypes=[ctypes.c_void_p,ctypes.c_uint,ctypes.c_size_t,ctypes.c_ssize_t]
user.ShowWindow.argtypes=[ctypes.c_void_p,ctypes.c_int]
kernel.OpenProcess.argtypes=[ctypes.c_uint32,ctypes.c_bool,ctypes.c_uint32];kernel.OpenProcess.restype=ctypes.c_void_p
kernel.QueryFullProcessImageNameW.argtypes=[ctypes.c_void_p,ctypes.c_uint32,ctypes.c_wchar_p,ctypes.POINTER(ctypes.c_uint32)]
kernel.CloseHandle.argtypes=[ctypes.c_void_p]


def windows(exe):
    found=[]
    @ctypes.WINFUNCTYPE(ctypes.c_bool,ctypes.c_void_p,ctypes.c_ssize_t)
    def visit(hwnd,_):
        text=ctypes.create_unicode_buffer(user.GetWindowTextLengthW(hwnd)+1);user.GetWindowTextW(hwnd,text,len(text))
        if text.value!='Tasks':return True
        pid=ctypes.c_uint32();user.GetWindowThreadProcessId(hwnd,ctypes.byref(pid))
        handle=kernel.OpenProcess(0x1000,False,pid.value)
        if handle:
            try:
                name=ctypes.create_unicode_buffer(32768);length=ctypes.c_uint32(len(name))
                if kernel.QueryFullProcessImageNameW(handle,0,name,ctypes.byref(length)) and Path(name.value).resolve()==exe:found.append(hwnd)
            finally:kernel.CloseHandle(handle)
        return True
    user.EnumWindows.argtypes=[type(visit),ctypes.c_ssize_t];user.EnumWindows(visit,0)
    return found


def wait_for(check,label,timeout=40):
    deadline=time.time()+timeout
    while time.time()<deadline:
        value=check()
        if value:return value
        time.sleep(.1)
    raise AssertionError(label)


def main():
    parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--exe',type=Path,required=True);args=parser.parse_args();exe=args.exe.resolve()
    with tempfile.TemporaryDirectory(prefix='tasks-window-qa-') as folder:
        data=Path(folder);env=dict(os.environ,TASKS_DATA_DIR=str(data));process=subprocess.Popen([str(exe),'--host','127.0.0.1','--port','0'],env=env)
        try:
            hwnd=wait_for(lambda:next(iter(windows(exe)),None),'Native Tasks window was not created')
            wait_for(lambda:window_properties(hwnd,read=[5]).get(5)==APP_USER_MODEL_ID,'Window AppUserModelID does not match the Tasks shortcut')
            properties=window_properties(hwnd,read=[2,3,4,5,9]);assert properties[2]=='"'+str(exe)+'"' and properties[5]==APP_USER_MODEL_ID and properties[9] is False
            assert str(exe) in properties[3] and properties[4]=='Tasks'
            wait_for(lambda:user.IsWindowVisible(hwnd),'Native Tasks window is not visible')
            # Relaunching a shortcut should activate this exact same window, never another host.
            launched=subprocess.run([str(exe)],env=env,timeout=30);assert launched.returncode==0
            assert windows(exe)==[hwnd],'Relaunch created a duplicate Tasks window'
            user.ShowWindow(hwnd,6);wait_for(lambda:user.IsIconic(hwnd),'Could not minimize the test window')
            subprocess.run([str(exe)],env=env,check=True,timeout=30)
            wait_for(lambda:not user.IsIconic(hwnd) and user.IsWindowVisible(hwnd),'Relaunch did not restore the existing window')
            user.PostMessageW(hwnd,0x0010,0,0)
            wait_for(lambda:not user.IsWindowVisible(hwnd),'Closing the window did not hide it')
            assert process.poll() is None,'Closing the window stopped local sync'
            subprocess.run([str(exe)],env=env,check=True,timeout=30)
            wait_for(lambda:user.IsWindowVisible(hwnd),'The pinned launch could not reopen the hidden window')
            assert windows(exe)==[hwnd],'Reopening a hidden app created another window'
            subprocess.run([str(exe),'--shutdown'],env=env,check=True,timeout=30);process.wait(timeout=20)
            assert process.returncode==0
            time.sleep(2)  # Let the WebView2 profile handles close before deleting test data.
            print(json.dumps({'passed':True,'checks':['Tasks executable owns the visible native window','window and shortcuts share AppUserModelID','taskbar relaunch targets the installed executable','taskbar pinning is allowed','relaunch reuses one window','relaunch restores a minimized window','closing hides the window and retains sync','relaunch restores a hidden window','shutdown closes window and server']}))
        finally:
            if process.poll() is None:
                subprocess.run(['taskkill','/PID',str(process.pid),'/T','/F'],stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL)
                process.wait(timeout=15)


if __name__=='__main__':main()
