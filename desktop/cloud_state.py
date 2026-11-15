"""Compact per-device change registers. Each client owns its own cloud document.

A phone seed is authoritative on first restore. Register clocks make replay
idempotent and concurrent independent edits commute, including deletions.
"""
import copy
import json
MISSING=object()
MAX_CLOCK=9007199254740991

def normalized(state):
    out=copy.deepcopy(state)
    for k in ['uiContext','updatedAt','inProgress']:out.pop(k,None)
    if 'customTasks'in out:out['customTasks']={t['id']:t for t in out['customTasks']}
    if 'inProgressTasks'in out:out['inProgressTasks']={v:True for v in out['inProgressTasks']}
    if 'manualLinks'in out:out['manualLinks']={k:{v:True for v in values}for k,values in out['manualLinks'].items()}
    return out

def restored(state):
    out=copy.deepcopy(state)
    if 'customTasks'in out:out['customTasks']=[out['customTasks'][key]for key in sorted(out['customTasks'])]
    if 'manualLinks'in out:out['manualLinks']={k:sorted(v for v,keep in values.items()if keep)for k,values in out['manualLinks'].items()}
    if 'inProgressTasks'in out:
        out['inProgressTasks']=sorted(v for v,keep in out['inProgressTasks'].items()if keep)
        out['inProgress']=out['inProgressTasks'][-1]if out['inProgressTasks']else None
    out['cloudManaged']=True
    return out

def pointer(path):return '/'+ '/'.join(str(v).replace('~','~0').replace('/','~1')for v in path)
def segments(path):
    if not isinstance(path,str)or not path.startswith('/')or len(path)>4000:raise ValueError('Invalid cloud change path')
    parts=[v.replace('~1','/').replace('~0','~')for v in path[1:].split('/')]
    if len(parts)>32 or any(v in ['__proto__','prototype','constructor']for v in parts):raise ValueError('Invalid cloud change path')
    return parts

def diff(before,after,path=()):
    if isinstance(after,dict):
        old=before if isinstance(before,dict)else{}
        result={}
        for k in sorted(set(old)|set(after)):
            if k not in after:result[pointer(path+(k,))]={'deleted':True}
            elif k not in old or old[k]!=after[k]:result.update(diff(old.get(k,MISSING),after[k],path+(k,)))
        return result
    if before is MISSING or before!=after:return{pointer(path):{'value':copy.deepcopy(after)}}
    return{}

def capture(journal,state):
    result=copy.deepcopy(journal)
    patch=diff(normalized(result.get('localHead',{})),normalized(state))
    if patch:
        clock=result.get('counter',0)+1
        if clock>MAX_CLOCK:raise ValueError('Cloud clock overflow')
        for key,op in patch.items():result.setdefault('registers',{})[key]={**op,'clock':clock}
        result['counter']=clock
    result['localHead']=copy.deepcopy(state)
    return result,bool(patch)

def resolve(seed,documents):
    state=normalized(seed);winners={};maximum=0
    for doc in documents:
        owner=doc['owner'];registers=doc['registers']
        if not isinstance(owner,str)or len(owner)>80 or not isinstance(registers,dict)or len(registers)>20000:raise ValueError('Invalid cloud device document')
        for path,op in registers.items():
            segments(path);clock=op.get('clock')
            if type(clock)is not int or not 0<clock<=MAX_CLOCK or not(op.get('deleted')is True or'value'in op):raise ValueError('Invalid cloud change')
            maximum=max(maximum,clock);key=(clock,owner,path)
            if path not in winners or key>winners[path][0]:winners[path]=(key,op)
    for path,(key,op)in sorted(winners.items(),key=lambda item:item[1][0]):
        names=segments(path);target=state
        for name in names[:-1]:
            if not isinstance(target.get(name),dict):target[name]={}
            target=target[name]
        if op.get('deleted')is True:target.pop(names[-1],None)
        else:target[names[-1]]=copy.deepcopy(op['value'])
    return restored(state),maximum
