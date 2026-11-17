// Serve packaged UI from a stable origin: file:// sessionStorage is undefined
// across browser process changes and does not model Android's native store.
import {createServer} from 'node:http';
import {readFile} from 'node:fs/promises';
import path from 'node:path';
import {once} from 'node:events';
export async function serveFixture(file){
 const root=path.dirname(path.resolve(file));
 const server=createServer(async(req,res)=>{
  try{
   const pathname=decodeURIComponent(new URL(req.url,'http://127.0.0.1').pathname);
   const target=pathname==='/'?path.resolve(file):path.resolve(root,'.'+pathname);
   if(!target.startsWith(root+path.sep)){res.writeHead(404);res.end();return;}
   const data=await readFile(target);res.setHeader('Content-Type',target.endsWith('.html')?'text/html; charset=utf-8':target.endsWith('.json')?'application/json':target.endsWith('.js')?'text/javascript':'application/octet-stream');res.end(data);
  }catch(_){res.writeHead(404);res.end();}
 });
 server.listen(0,'127.0.0.1');await once(server,'listening');
 return{url:'http://127.0.0.1:'+server.address().port+'/',close:()=>new Promise((resolve,reject)=>server.close(error=>error?reject(error):resolve()))};
}
