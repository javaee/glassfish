(function(){var $wnd = window;var $doc = $wnd.document;var $moduleName, $moduleBase;var _,ox='com.google.gwt.core.client.',px='com.google.gwt.http.client.',qx='com.google.gwt.lang.',rx='com.google.gwt.user.client.',sx='com.google.gwt.user.client.impl.',tx='com.google.gwt.user.client.ui.',ux='com.sun.javaone.client.',vx='java.lang.',wx='java.util.';function nx(){}
function qr(a){return this===a;}
function rr(){return ns(this);}
function or(){}
_=or.prototype={};_.eQ=qr;_.hC=rr;_.tI=1;function o(){return u();}
var p=null;function s(a){return a==null?0:a.$H?a.$H:(a.$H=v());}
function t(a){return a==null?0:a.$H?a.$H:(a.$H=v());}
function u(){var b=$doc.location.href;var a=b.indexOf('#');if(a!= -1)b=b.substring(0,a);a=b.indexOf('?');if(a!= -1)b=b.substring(0,a);a=b.lastIndexOf('/');if(a!= -1)b=b.substring(0,a);return b.length>0?b+'/':'';}
function v(){return ++w;}
var w=0;function ps(b,a){a;return b;}
function rs(b,a){if(b.a!==null){throw Dq(new Cq(),"Can't overwrite cause");}if(a===b){throw Aq(new zq(),'Self-causation not permitted');}b.a=a;return b;}
function os(){}
_=os.prototype=new or();_.tI=3;_.a=null;function xq(b,a){ps(b,a);return b;}
function wq(){}
_=wq.prototype=new os();_.tI=4;function tr(b,a){xq(b,a);return b;}
function sr(){}
_=sr.prototype=new wq();_.tI=5;function y(c,b,a){tr(c,'JavaScript '+b+' exception: '+a);return c;}
function x(){}
_=x.prototype=new sr();_.tI=6;function C(b,a){if(!kd(a,2)){return false;}return bb(b,jd(a,2));}
function D(a){return s(a);}
function E(){return [];}
function F(){return function(){};}
function ab(){return {};}
function cb(a){return C(this,a);}
function bb(a,b){return a===b;}
function db(){return D(this);}
function A(){}
_=A.prototype=new or();_.eQ=cb;_.hC=db;_.tI=7;function ec(b,d,c,a){if(d===null){throw new hr();}if(a===null){throw new hr();}if(c<0){throw new zq();}b.a=c;b.c=d;if(c>0){b.b=lb(new kb(),b,a);eg(b.b,c);}else{b.b=null;}return b;}
function gc(a){var b;if(a.c!==null){b=a.c;a.c=null;vc(b);fc(a);}}
function fc(a){if(a.b!==null){bg(a.b);}}
function ic(e,a){var b,c,d,f;if(e.c===null){return;}fc(e);f=e.c;e.c=null;b=wc(f);if(b!==null){c=tr(new sr(),b);a.C(e,c);}else{d=kc(f);a.F(e,d);}}
function jc(b,a){if(b.c===null){return;}gc(b);a.C(b,bc(new ac(),b,b.a));}
function kc(b){var a;a=gb(new fb(),b);return a;}
function lc(a){var b;b=p;{ic(this,a);}}
function eb(){}
_=eb.prototype=new or();_.q=lc;_.tI=0;_.a=0;_.b=null;_.c=null;function mc(){}
_=mc.prototype=new or();_.tI=0;function gb(a,b){a.a=b;return a;}
function ib(a){return yc(a.a);}
function jb(a){return xc(a.a);}
function fb(){}
_=fb.prototype=new mc();_.tI=0;function cg(){cg=nx;kg=Au(new yu());{jg();}}
function ag(a){cg();return a;}
function bg(a){if(a.c){fg(a.d);}else{gg(a.d);}dv(kg,a);}
function dg(a){if(!a.c){dv(kg,a);}a.hb();}
function eg(b,a){if(a<=0){throw Aq(new zq(),'must be positive');}bg(b);b.c=false;b.d=hg(b,a);Cu(kg,b);}
function fg(a){cg();$wnd.clearInterval(a);}
function gg(a){cg();$wnd.clearTimeout(a);}
function hg(b,a){cg();return $wnd.setTimeout(function(){b.r();},a);}
function ig(){var a;a=p;{dg(this);}}
function jg(){cg();pg(new Cf());}
function Bf(){}
_=Bf.prototype=new or();_.r=ig;_.tI=8;_.c=false;_.d=0;var kg;function mb(){mb=nx;cg();}
function lb(b,a,c){mb();b.a=a;b.b=c;ag(b);return b;}
function nb(){jc(this.a,this.b);}
function kb(){}
_=kb.prototype=new Bf();_.hb=nb;_.tI=9;function ub(){ub=nx;xb=qb(new pb(),'GET');qb(new pb(),'POST');yb=Fh(new Eh());}
function sb(b,a,c){ub();tb(b,a===null?null:a.a,c);return b;}
function tb(b,a,c){ub();qc('httpMethod',a);qc('url',c);b.a=a;b.c=c;return b;}
function vb(g,d,a){var b,c,e,f,h;h=bi(yb);{b=zc(h,g.a,g.c,true);}if(b!==null){e=Eb(new Db(),g.c);rs(e,Bb(new Ab(),b));throw e;}wb(g,h);c=ec(new eb(),h,g.b,a);f=Ac(h,c,d,a);if(f!==null){throw Bb(new Ab(),f);}return c;}
function wb(a,b){{Bc(b,'Content-Type','text/plain; charset=utf-8');}}
function ob(){}
_=ob.prototype=new or();_.tI=0;_.a=null;_.b=0;_.c=null;var xb,yb;function qb(b,a){b.a=a;return b;}
function pb(){}
_=pb.prototype=new or();_.tI=0;_.a=null;function Bb(b,a){xq(b,a);return b;}
function Ab(){}
_=Ab.prototype=new wq();_.tI=10;function Eb(a,b){Bb(a,'The URL '+b+' is invalid or violates the same-origin security restriction');return a;}
function Db(){}
_=Db.prototype=new Ab();_.tI=11;function bc(b,a,c){Bb(b,dc(c));return b;}
function dc(a){return 'A request timeout has expired after '+er(a)+' ms';}
function ac(){}
_=ac.prototype=new Ab();_.tI=12;function qc(a,b){rc(a,b);if(0==Dr(es(b))){throw Aq(new zq(),a+' can not be empty');}}
function rc(a,b){if(null===b){throw ir(new hr(),a+' can not be null');}}
function vc(a){a.onreadystatechange=di;a.abort();}
function wc(b){try{if(b.status===undefined){return 'XmlHttpRequest.status == undefined, please see Safari bug '+'http://bugs.webkit.org/show_bug.cgi?id=3810 for more details';}return null;}catch(a){return 'Unable to read XmlHttpRequest.status; likely causes are a '+'networking error or bad cross-domain request. Please see '+'https://bugzilla.mozilla.org/show_bug.cgi?id=238559 for more '+'details';}}
function xc(a){return a.responseText;}
function yc(a){return a.status;}
function zc(e,c,d,b){try{e.open(c,d,b);return null;}catch(a){return a.message||a.toString();}}
function Ac(e,c,d,b){e.onreadystatechange=function(){if(e.readyState==uc){e.onreadystatechange=di;c.q(b);}};try{e.send(d);return null;}catch(a){e.onreadystatechange=di;return a.message||a.toString();}}
function Bc(d,b,c){try{d.setRequestHeader(b,c);return null;}catch(a){return a.message||a.toString();}}
var uc=4;function Dc(c,a,d,b,e){c.a=a;c.b=b;e;c.tI=d;return c;}
function Fc(a,b,c){return a[b]=c;}
function ad(b,a){return b[a];}
function bd(a){return a.length;}
function dd(e,d,c,b,a){return cd(e,d,c,b,0,bd(b),a);}
function cd(j,i,g,c,e,a,b){var d,f,h;if((f=ad(c,e))<0){throw new fr();}h=Dc(new Cc(),f,ad(i,e),ad(g,e),j);++e;if(e<a){j=bs(j,1);for(d=0;d<f;++d){Fc(h,d,cd(j,i,g,c,e,a,b));}}else{for(d=0;d<f;++d){Fc(h,d,b);}}return h;}
function ed(a,b,c){if(c!==null&&a.b!=0&& !kd(c,a.b)){throw new mq();}return Fc(a,b,c);}
function Cc(){}
_=Cc.prototype=new or();_.tI=0;function hd(b,a){return !(!(b&&nd[b][a]));}
function id(a){return String.fromCharCode(a);}
function jd(b,a){if(b!=null)hd(b.tI,a)||md();return b;}
function kd(b,a){return b!=null&&hd(b.tI,a);}
function md(){throw new sq();}
function ld(a){if(a!==null){throw new sq();}return a;}
function od(b,d){_=d.prototype;if(b&& !(b.tI>=_.tI)){var c=b.toString;for(var a in _){b[a]=_[a];}b.toString=c;}return b;}
var nd;function rd(a){if(kd(a,3)){return a;}return y(new x(),td(a),sd(a));}
function sd(a){return a.message;}
function td(a){return a.name;}
function xd(){if(wd===null||Ad()){wd=lw(new rv());zd(wd);}return wd;}
function yd(b){var a;a=xd();return jd(rw(a,b),1);}
function zd(e){var b=$doc.cookie;if(b&&b!=''){var a=b.split('; ');for(var d=0;d<a.length;++d){var f,g;var c=a[d].indexOf('=');if(c== -1){f=a[d];g='';}else{f=a[d].substring(0,c);g=a[d].substring(c+1);}f=decodeURIComponent(f);g=decodeURIComponent(g);e.eb(f,g);}}}
function Ad(){var a=$doc.cookie;if(a!=''&&a!=Bd){Bd=a;return true;}else{return false;}}
function Cd(a){$doc.cookie=a+"='';expires='Fri, 02-Jan-1970 00:00:00 GMT'";}
function Ed(a,b){Dd(a,b,0,null,null,false);}
function Dd(d,g,c,b,e,f){var a=encodeURIComponent(d)+'='+encodeURIComponent(g);if(c)a+=';expires='+new Date(c).toGMTString();if(b)a+=';domain='+b;if(e)a+=';path='+e;if(f)a+=';secure';$doc.cookie=a;}
var wd=null,Bd=null;function ae(){ae=nx;De=Au(new yu());{ye=new Eg();hh(ye);}}
function be(b,a){ae();kh(ye,b,a);}
function ce(a,b){ae();return ch(ye,a,b);}
function de(){ae();return mh(ye,'A');}
function ee(){ae();return mh(ye,'div');}
function fe(){ae();return mh(ye,'tbody');}
function ge(){ae();return mh(ye,'td');}
function he(){ae();return mh(ye,'tr');}
function ie(){ae();return mh(ye,'table');}
function le(b,a,d){ae();var c;c=p;{ke(b,a,d);}}
function ke(b,a,c){ae();var d;if(a===Ce){if(ne(b)==8192){Ce=null;}}d=je;je=b;try{c.B(b);}finally{je=d;}}
function me(b,a){ae();nh(ye,b,a);}
function ne(a){ae();return oh(ye,a);}
function oe(a){ae();dh(ye,a);}
function pe(b,a){ae();return ph(ye,b,a);}
function qe(a){ae();return qh(ye,a);}
function se(a,b){ae();return sh(ye,a,b);}
function re(a,b){ae();return rh(ye,a,b);}
function te(a){ae();return th(ye,a);}
function ue(a){ae();return eh(ye,a);}
function ve(a){ae();return uh(ye,a);}
function we(a){ae();return fh(ye,a);}
function xe(a){ae();return gh(ye,a);}
function ze(c,a,b){ae();ih(ye,c,a,b);}
function Ae(a){ae();var b,c;c=true;if(De.b>0){b=ld(Fu(De,De.b-1));if(!(c=null.lb())){me(a,true);oe(a);}}return c;}
function Be(b,a){ae();vh(ye,b,a);}
function Ee(a,b,c){ae();wh(ye,a,b,c);}
function Fe(a,b){ae();xh(ye,a,b);}
function af(a,b){ae();yh(ye,a,b);}
function bf(a,b){ae();zh(ye,a,b);}
function cf(b,a,c){ae();Ah(ye,b,a,c);}
function df(a,b){ae();jh(ye,a,b);}
function ef(){ae();return Bh(ye);}
function ff(){ae();return Ch(ye);}
var je=null,ye=null,Ce=null,De;function jf(a){if(kd(a,4)){return ce(this,jd(a,4));}return C(od(this,gf),a);}
function kf(){return D(od(this,gf));}
function gf(){}
_=gf.prototype=new A();_.eQ=jf;_.hC=kf;_.tI=13;function of(a){return C(od(this,lf),a);}
function pf(){return D(od(this,lf));}
function lf(){}
_=lf.prototype=new A();_.eQ=of;_.hC=pf;_.tI=14;function sf(){sf=nx;xf=Au(new yu());{yf=new fi();if(!hi(yf)){yf=null;}}}
function tf(a){sf();Cu(xf,a);}
function uf(){sf();$wnd.history.back();}
function vf(a){sf();var b,c;for(b=ft(xf);Es(b);){c=jd(Fs(b),5);c.D(a);}}
function wf(){sf();return yf!==null?ki(yf):'';}
function zf(a){sf();if(yf!==null){ii(yf,a);}}
function Af(b){sf();var a;a=p;{vf(b);}}
var xf,yf=null;function Ef(){while((cg(),kg).b>0){bg(jd(Fu((cg(),kg),0),6));}}
function Ff(){return null;}
function Cf(){}
_=Cf.prototype=new or();_.bb=Ef;_.cb=Ff;_.tI=15;function og(){og=nx;rg=Au(new yu());Bg=Au(new yu());{xg();}}
function pg(a){og();Cu(rg,a);}
function qg(a){og();Cu(Bg,a);}
function sg(){og();var a,b;for(a=ft(rg);Es(a);){b=jd(Fs(a),7);b.bb();}}
function tg(){og();var a,b,c,d;d=null;for(a=ft(rg);Es(a);){b=jd(Fs(a),7);c=b.cb();{d=c;}}return d;}
function ug(){og();var a,b;for(a=ft(Bg);Es(a);){b=jd(Fs(a),8);b.db(wg(),vg());}}
function vg(){og();return ef();}
function wg(){og();return ff();}
function xg(){og();__gwt_initHandlers(function(){Ag();},function(){return zg();},function(){yg();$wnd.onresize=null;$wnd.onbeforeclose=null;$wnd.onclose=null;});}
function yg(){og();var a;a=p;{sg();}}
function zg(){og();var a;a=p;{return tg();}}
function Ag(){og();var a;a=p;{ug();}}
function Cg(a){og();$doc.title=a;}
var rg,Bg;function kh(c,b,a){b.appendChild(a);}
function mh(b,a){return $doc.createElement(a);}
function nh(c,b,a){b.cancelBubble=a;}
function oh(b,a){switch(a.type){case 'blur':return 4096;case 'change':return 1024;case 'click':return 1;case 'dblclick':return 2;case 'focus':return 2048;case 'keydown':return 128;case 'keypress':return 256;case 'keyup':return 512;case 'load':return 32768;case 'losecapture':return 8192;case 'mousedown':return 4;case 'mousemove':return 64;case 'mouseout':return 32;case 'mouseover':return 16;case 'mouseup':return 8;case 'scroll':return 16384;case 'error':return 65536;case 'mousewheel':return 131072;case 'DOMMouseScroll':return 131072;}}
function ph(d,b,a){var c=b.getAttribute(a);return c==null?null:c;}
function qh(c,b){var a=$doc.getElementById(b);return a||null;}
function sh(d,a,b){var c=a[b];return c==null?null:String(c);}
function rh(d,a,c){var b=parseInt(a[c]);if(!b){return 0;}return b;}
function th(b,a){return a.__eventBits||0;}
function uh(d,b){var c='',a=b.firstChild;while(a){if(a.nodeType==1){c+=d.s(a);}else if(a.nodeValue){c+=a.nodeValue;}a=a.nextSibling;}return c;}
function vh(c,b,a){b.removeChild(a);}
function wh(c,a,b,d){a[b]=d;}
function xh(c,a,b){a.__listener=b;}
function yh(c,a,b){if(!b){b='';}a.innerHTML=b;}
function zh(c,a,b){while(a.firstChild){a.removeChild(a.firstChild);}if(b!=null){a.appendChild($doc.createTextNode(b));}}
function Ah(c,b,a,d){b.style[a]=d;}
function Bh(a){return $doc.body.clientHeight;}
function Ch(a){return $doc.body.clientWidth;}
function Dh(a){return uh(this,a);}
function Dg(){}
_=Dg.prototype=new or();_.s=Dh;_.tI=0;function ch(c,a,b){return a==b;}
function dh(b,a){a.preventDefault();}
function eh(c,b){var a=b.firstChild;while(a&&a.nodeType!=1)a=a.nextSibling;return a||null;}
function fh(c,a){var b=a.nextSibling;while(b&&b.nodeType!=1)b=b.nextSibling;return b||null;}
function gh(c,a){var b=a.parentNode;if(b==null){return null;}if(b.nodeType!=1)b=null;return b||null;}
function hh(d){$wnd.__dispatchCapturedMouseEvent=function(b){if($wnd.__dispatchCapturedEvent(b)){var a=$wnd.__captureElem;if(a&&a.__listener){le(b,a,a.__listener);b.stopPropagation();}}};$wnd.__dispatchCapturedEvent=function(a){if(!Ae(a)){a.stopPropagation();a.preventDefault();return false;}return true;};$wnd.addEventListener('click',$wnd.__dispatchCapturedMouseEvent,true);$wnd.addEventListener('dblclick',$wnd.__dispatchCapturedMouseEvent,true);$wnd.addEventListener('mousedown',$wnd.__dispatchCapturedMouseEvent,true);$wnd.addEventListener('mouseup',$wnd.__dispatchCapturedMouseEvent,true);$wnd.addEventListener('mousemove',$wnd.__dispatchCapturedMouseEvent,true);$wnd.addEventListener('mousewheel',$wnd.__dispatchCapturedMouseEvent,true);$wnd.addEventListener('keydown',$wnd.__dispatchCapturedEvent,true);$wnd.addEventListener('keyup',$wnd.__dispatchCapturedEvent,true);$wnd.addEventListener('keypress',$wnd.__dispatchCapturedEvent,true);$wnd.__dispatchEvent=function(b){var c,a=this;while(a&& !(c=a.__listener))a=a.parentNode;if(a&&a.nodeType!=1)a=null;if(c)le(b,a,c);};$wnd.__captureElem=null;}
function ih(f,e,g,d){var c=0,b=e.firstChild,a=null;while(b){if(b.nodeType==1){if(c==d){a=b;break;}++c;}b=b.nextSibling;}e.insertBefore(g,a);}
function jh(c,b,a){b.__eventBits=a;b.onclick=a&1?$wnd.__dispatchEvent:null;b.ondblclick=a&2?$wnd.__dispatchEvent:null;b.onmousedown=a&4?$wnd.__dispatchEvent:null;b.onmouseup=a&8?$wnd.__dispatchEvent:null;b.onmouseover=a&16?$wnd.__dispatchEvent:null;b.onmouseout=a&32?$wnd.__dispatchEvent:null;b.onmousemove=a&64?$wnd.__dispatchEvent:null;b.onkeydown=a&128?$wnd.__dispatchEvent:null;b.onkeypress=a&256?$wnd.__dispatchEvent:null;b.onkeyup=a&512?$wnd.__dispatchEvent:null;b.onchange=a&1024?$wnd.__dispatchEvent:null;b.onfocus=a&2048?$wnd.__dispatchEvent:null;b.onblur=a&4096?$wnd.__dispatchEvent:null;b.onlosecapture=a&8192?$wnd.__dispatchEvent:null;b.onscroll=a&16384?$wnd.__dispatchEvent:null;b.onload=a&32768?$wnd.__dispatchEvent:null;b.onerror=a&65536?$wnd.__dispatchEvent:null;b.onmousewheel=a&131072?$wnd.__dispatchEvent:null;}
function ah(){}
_=ah.prototype=new Dg();_.tI=0;function Eg(){}
_=Eg.prototype=new ah();_.tI=0;function Fh(a){di=F();return a;}
function bi(a){return ci(a);}
function ci(a){return new XMLHttpRequest();}
function Eh(){}
_=Eh.prototype=new or();_.tI=0;var di=null;function ki(a){return $wnd.__gwt_historyToken;}
function li(a){Af(a);}
function ei(){}
_=ei.prototype=new or();_.tI=0;function hi(d){$wnd.__gwt_historyToken='';var c=$wnd.location.hash;if(c.length>0)$wnd.__gwt_historyToken=c.substring(1);$wnd.__checkHistory=function(){var b='',a=$wnd.location.hash;if(a.length>0)b=a.substring(1);if(b!=$wnd.__gwt_historyToken){$wnd.__gwt_historyToken=b;li(b);}$wnd.setTimeout('__checkHistory()',250);};$wnd.__checkHistory();return true;}
function ii(b,a){if(a==null){a='';}$wnd.location.hash=encodeURIComponent(a);}
function fi(){}
_=fi.prototype=new ei();_.tI=0;function am(b,a){bm(b,em(b)+id(45)+a);}
function bm(b,a){qm(b.i,a,true);}
function dm(a){return re(a.i,'offsetWidth');}
function em(a){return om(a.i);}
function fm(b,a){gm(b,em(b)+id(45)+a);}
function gm(b,a){qm(b.i,a,false);}
function hm(d,b,a){var c=b.parentNode;if(!c){return;}c.insertBefore(a,b);c.removeChild(b);}
function im(b,a){if(b.i!==null){hm(b,b.i,a);}b.i=a;}
function jm(b,a){pm(b.i,a);}
function km(b,a){rm(b.i,a);}
function lm(a,b){sm(a.i,b);}
function mm(b,a){df(b.i,a|te(b.i));}
function nm(a){return se(a,'className');}
function om(a){var b,c;b=nm(a);c=Ar(b,32);if(c>=0){return cs(b,0,c);}return b;}
function pm(a,b){Ee(a,'className',b);}
function qm(c,j,a){var b,d,e,f,g,h,i;if(c===null){throw tr(new sr(),'Null widget handle. If you are creating a composite, ensure that initWidget() has been called.');}j=es(j);if(Dr(j)==0){throw Aq(new zq(),'Style names cannot be empty');}i=nm(c);e=Br(i,j);while(e!=(-1)){if(e==0||wr(i,e-1)==32){f=e+Dr(j);g=Dr(i);if(f==g||f<g&&wr(i,f)==32){break;}}e=Cr(i,j,e+1);}if(a){if(e==(-1)){if(Dr(i)>0){i+=' ';}Ee(c,'className',i+j);}}else{if(e!=(-1)){b=es(cs(i,0,e));d=es(bs(i,e+Dr(j)));if(Dr(b)==0){h=d;}else if(Dr(d)==0){h=b;}else{h=b+' '+d;}Ee(c,'className',h);}}}
function rm(a,b){if(a===null){throw tr(new sr(),'Null widget handle. If you are creating a composite, ensure that initWidget() has been called.');}b=es(b);if(Dr(b)==0){throw Aq(new zq(),'Style names cannot be empty');}tm(a,b);}
function sm(a,b){a.style.display=b?'':'none';}
function tm(b,f){var a=b.className.split(/\s+/);if(!a){return;}var g=a[0];var h=g.length;a[0]=f;for(var c=1,d=a.length;c<d;c++){var e=a[c];if(e.length>h&&(e.charAt(h)=='-'&&e.indexOf(g)==0)){a[c]=f+e.substring(h);}}b.className=a.join(' ');}
function Fl(){}
_=Fl.prototype=new or();_.tI=0;_.i=null;function on(a){if(a.g){throw Dq(new Cq(),"Should only call onAttach when the widget is detached from the browser's document");}a.g=true;Fe(a.i,a);a.n();a.E();}
function pn(a){if(!a.g){throw Dq(new Cq(),"Should only call onDetach when the widget is attached to the browser's document");}try{a.ab();}finally{a.o();Fe(a.i,null);a.g=false;}}
function qn(a){if(a.h!==null){a.h.gb(a);}else if(a.h!==null){throw Dq(new Cq(),"This widget's parent does not implement HasWidgets");}}
function rn(b,a){if(b.g){Fe(b.i,null);}im(b,a);if(b.g){Fe(a,b);}}
function sn(c,b){var a;a=c.h;if(b===null){if(a!==null&&a.g){pn(c);}c.h=null;}else{if(a!==null){throw Dq(new Cq(),'Cannot set a new parent without first clearing the old parent');}c.h=b;if(b.g){on(c);}}}
function tn(){}
function un(){}
function vn(a){}
function wn(){}
function xn(){}
function Cm(){}
_=Cm.prototype=new Fl();_.n=tn;_.o=un;_.B=vn;_.E=wn;_.ab=xn;_.tI=16;_.g=false;_.h=null;function Ak(b,a){sn(a,b);}
function Ck(b,a){sn(a,null);}
function Dk(){var a,b;for(b=this.y();b.x();){a=jd(b.A(),9);on(a);}}
function Ek(){var a,b;for(b=this.y();b.x();){a=jd(b.A(),9);pn(a);}}
function Fk(){}
function al(){}
function zk(){}
_=zk.prototype=new Cm();_.n=Dk;_.o=Ek;_.E=Fk;_.ab=al;_.tI=17;function yi(a){a.f=dn(new Dm(),a);}
function zi(a){yi(a);return a;}
function Ai(c,a,b){qn(a);en(c.f,a);be(b,a.i);Ak(c,a);}
function Bi(d,b,a){var c;Di(d,a);if(b.h===d){c=Fi(d,b);if(c<a){a--;}}return a;}
function Ci(b,a){if(a<0||a>=b.f.b){throw new Fq();}}
function Di(b,a){if(a<0||a>b.f.b){throw new Fq();}}
function aj(b,a){return gn(b.f,a);}
function Fi(b,a){return hn(b.f,a);}
function bj(e,b,c,a,d){a=Bi(e,b,a);qn(b);jn(e.f,b,a);if(d){ze(c,b.i,a);}else{be(c,b.i);}Ak(e,b);}
function cj(b,a){return b.gb(aj(b,a));}
function dj(b,c){var a;if(c.h!==b){return false;}Ck(b,c);a=c.i;Be(xe(a),a);mn(b.f,c);return true;}
function ej(){return kn(this.f);}
function fj(a){return dj(this,a);}
function xi(){}
_=xi.prototype=new zk();_.y=ej;_.gb=fj;_.tI=18;function ni(a){zi(a);rn(a,ee());cf(a.i,'position','relative');cf(a.i,'overflow','hidden');return a;}
function oi(a,b){Ai(a,b,a.i);}
function qi(a){cf(a,'left','');cf(a,'top','');cf(a,'position','');}
function ri(b){var a;a=dj(this,b);if(a){qi(b.i);}return a;}
function mi(){}
_=mi.prototype=new xi();_.gb=ri;_.tI=19;function ti(a){zi(a);a.e=ie();a.d=fe();be(a.e,a.d);rn(a,a.e);return a;}
function vi(c,b,a){Ee(b,'align',a.a);}
function wi(c,b,a){cf(b,'verticalAlign',a.a);}
function si(){}
_=si.prototype=new xi();_.tI=20;_.d=null;_.e=null;function hj(a){zi(a);rn(a,ee());return a;}
function ij(a,b){Ai(a,b,a.i);kj(a,b);}
function kj(b,c){var a;a=c.i;cf(a,'width','100%');cf(a,'height','100%');lm(c,false);}
function lj(a,b){cf(b.i,'width','');cf(b.i,'height','');lm(b,true);}
function mj(b,a){Ci(b,a);if(b.a!==null){lm(b.a,false);}b.a=aj(b,a);lm(b.a,true);}
function nj(b){var a;a=dj(this,b);if(a){lj(this,b);if(this.a===b){this.a=null;}}return a;}
function gj(){}
_=gj.prototype=new xi();_.gb=nj;_.tI=21;_.a=null;function wk(a){rn(a,ee());mm(a,131197);jm(a,'gwt-Label');return a;}
function yk(a){switch(ne(a)){case 1:break;case 4:case 8:case 64:case 16:case 32:break;case 131072:break;}}
function vk(){}
_=vk.prototype=new Cm();_.B=yk;_.tI=22;function pj(a){wk(a);rn(a,ee());mm(a,125);jm(a,'gwt-HTML');return a;}
function qj(b,a){pj(b);sj(b,a);return b;}
function sj(b,a){af(b.i,a);}
function oj(){}
_=oj.prototype=new vk();_.tI=23;function yj(){yj=nx;wj(new vj(),'center');zj=wj(new vj(),'left');wj(new vj(),'right');}
var zj;function wj(b,a){b.a=a;return b;}
function vj(){}
_=vj.prototype=new or();_.tI=0;_.a=null;function Ej(){Ej=nx;Fj=Cj(new Bj(),'bottom');Cj(new Bj(),'middle');ak=Cj(new Bj(),'top');}
var Fj,ak;function Cj(a,b){a.a=b;return a;}
function Bj(){}
_=Bj.prototype=new or();_.tI=0;_.a=null;function ek(a){a.a=(yj(),zj);a.c=(Ej(),ak);}
function fk(a){ti(a);ek(a);a.b=he();be(a.d,a.b);Ee(a.e,'cellSpacing','0');Ee(a.e,'cellPadding','0');return a;}
function gk(b,c){var a;a=ik(b);be(b.b,a);Ai(b,c,a);}
function ik(b){var a;a=ge();vi(b,a,b.a);wi(b,a,b.c);return a;}
function jk(c,d,a){var b;Di(c,a);b=ik(c);ze(c.b,b,a);bj(c,d,b,a,false);}
function kk(c,d){var a,b;b=xe(d.i);a=dj(c,d);if(a){Be(c.b,b);}return a;}
function lk(b,a){b.c=a;}
function mk(a){return kk(this,a);}
function dk(){}
_=dk.prototype=new si();_.gb=mk;_.tI=24;_.b=null;function ok(a){rn(a,ee());be(a.i,a.a=de());mm(a,1);jm(a,'gwt-Hyperlink');return a;}
function pk(c,b,a){ok(c);sk(c,b);rk(c,a);return c;}
function rk(b,a){b.b=a;Ee(b.a,'href','#'+a);}
function sk(b,a){bf(b.a,a);}
function tk(a){if(ne(a)==1){zf(this.b);oe(a);}}
function nk(){}
_=nk.prototype=new Cm();_.B=tk;_.tI=25;_.a=null;_.b=null;function hl(){hl=nx;ml=lw(new rv());}
function gl(b,a){hl();ni(b);if(a===null){a=il();}rn(b,a);on(b);return b;}
function jl(){hl();return kl(null);}
function kl(c){hl();var a,b;b=jd(rw(ml,c),10);if(b!==null){return b;}a=null;if(c!==null){if(null===(a=qe(c))){return null;}}if(ml.c==0){ll();}sw(ml,c,b=gl(new bl(),a));return b;}
function il(){hl();return $doc.body;}
function ll(){hl();pg(new cl());}
function bl(){}
_=bl.prototype=new mi();_.tI=26;var ml;function el(){var a,b;for(b=Et(mu((hl(),ml)));fu(b);){a=jd(gu(b),10);if(a.g){pn(a);}}}
function fl(){return null;}
function cl(){}
_=cl.prototype=new or();_.bb=el;_.cb=fl;_.tI=27;function ul(a){vl(a,ee());return a;}
function vl(b,a){rn(b,a);return b;}
function wl(a,b){if(a.a!==null){throw Dq(new Cq(),'SimplePanel can only contain one child widget');}zl(a,b);}
function yl(a,b){if(a.a!==b){return false;}Ck(a,b);Be(a.i,b.i);a.a=null;return true;}
function zl(a,b){if(b===a.a){return;}if(b!==null){qn(b);}if(a.a!==null){yl(a,a.a);}a.a=b;if(b!==null){be(a.i,a.a.i);Ak(a,b);}}
function Al(){return ql(new ol(),this);}
function Bl(a){return yl(this,a);}
function nl(){}
_=nl.prototype=new zk();_.y=Al;_.gb=Bl;_.tI=28;_.a=null;function pl(a){a.a=a.b.a!==null;}
function ql(b,a){b.b=a;pl(b);return b;}
function sl(){return this.a;}
function tl(){if(!this.a||this.b.a===null){throw new jx();}this.a=false;return this.b.a;}
function ol(){}
_=ol.prototype=new or();_.x=sl;_.A=tl;_.tI=0;function vm(a){a.a=(yj(),zj);a.b=(Ej(),ak);}
function wm(a){ti(a);vm(a);Ee(a.e,'cellSpacing','0');Ee(a.e,'cellPadding','0');return a;}
function xm(b,d){var a,c;c=he();a=zm(b);be(c,a);be(b.d,c);Ai(b,d,a);}
function zm(b){var a;a=ge();vi(b,a,b.a);wi(b,a,b.b);return a;}
function Am(c,e,a){var b,d;Di(c,a);d=he();b=zm(c);be(d,b);ze(c.d,d,a);bj(c,e,b,a,false);}
function Bm(c){var a,b;b=xe(c.i);a=dj(this,c);if(a){Be(this.d,xe(b));}return a;}
function um(){}
_=um.prototype=new si();_.gb=Bm;_.tI=29;function dn(b,a){b.a=dd('[Lcom.google.gwt.user.client.ui.Widget;',[0],[9],[4],null);return b;}
function en(a,b){jn(a,b,a.b);}
function gn(b,a){if(a<0||a>=b.b){throw new Fq();}return b.a[a];}
function hn(b,c){var a;for(a=0;a<b.b;++a){if(b.a[a]===c){return a;}}return (-1);}
function jn(d,e,a){var b,c;if(a<0||a>d.b){throw new Fq();}if(d.b==d.a.a){c=dd('[Lcom.google.gwt.user.client.ui.Widget;',[0],[9],[d.a.a*2],null);for(b=0;b<d.a.a;++b){ed(c,b,d.a[b]);}d.a=c;}++d.b;for(b=d.b-1;b>a;--b){ed(d.a,b,d.a[b-1]);}ed(d.a,a,e);}
function kn(a){return Fm(new Em(),a);}
function ln(c,b){var a;if(b<0||b>=c.b){throw new Fq();}--c.b;for(a=b;a<c.b;++a){ed(c.a,a,c.a[a+1]);}ed(c.a,c.b,null);}
function mn(b,c){var a;a=hn(b,c);if(a==(-1)){throw new jx();}ln(b,a);}
function Dm(){}
_=Dm.prototype=new or();_.tI=0;_.a=null;_.b=0;function Fm(b,a){b.b=a;return b;}
function bn(){return this.a<this.b.b-1;}
function cn(){if(this.a>=this.b.b){throw new jx();}return this.b.a[++this.a];}
function Em(){}
_=Em.prototype=new or();_.x=bn;_.A=cn;_.tI=0;_.a=(-1);function Fo(){Fo=nx;qp=ds('abcdefghijklmnopqrstuvwxyz');}
function Co(a){a.a=yp(new tp());}
function Do(a){Fo();Co(a);return a;}
function Eo(a){qg(An(new zn(),a));}
function ap(c,a){var b;b=jb(a);return (ib(a)==200||ib(a)==203||ib(a)<100)&&b!==null&& !zr(b,'');}
function bp(e,d){var a,c,f;f=o()+'/appendix'+id(qp[d])+'.html';c=sb(new ob(),(ub(),xb),f);try{vb(c,null,to(new so(),e,d,f));}catch(a){a=rd(a);if(kd(a,14)){}else throw a;}}
function cp(e,d){var a,c,f;f=o()+'/exercise'+d+'.html';c=sb(new ob(),(ub(),xb),f);try{vb(c,null,En(new Dn(),e,d,f));}catch(a){a=rd(a);if(kd(a,14)){a;fp(e);}else throw a;}}
function dp(d){var a,c,e;e=o()+'/intro.html';c=sb(new ob(),(ub(),xb),e);try{vb(c,null,jo(new io(),d,e));}catch(a){a=rd(a);if(kd(a,14)){a;cp(d,0);}else throw a;}}
function ep(e,d){var a,c,f;f=o()+'/solution'+d+'.html';c=sb(new ob(),(ub(),xb),f);try{vb(c,null,eo(new co(),e,d,f));}catch(a){a=rd(a);if(kd(a,14)){a;cp(e,d+1);}else throw a;}}
function fp(d){var a,c,e;e=o()+'/summary.html';c=sb(new ob(),(ub(),xb),e);try{vb(c,null,oo(new no(),d,e));}catch(a){a=rd(a);if(kd(a,14)){a;op(d);bp(d,0);}else throw a;}}
function gp(e,d,f){var a,c;c=sb(new ob(),(ub(),xb),f);try{vb(c,null,yo(new xo(),e,d));}catch(a){a=rd(a);if(kd(a,14)){}else throw a;}}
function hp(d,c){var a,b,e,f;b=Er(c,',');for(a=0;a<b.a;a++){if(!zr(b[a],'')){e=mp(d,b[a]);f=np(d,b[a]);Dp(d.a,b[a],e,null);if(f!==null&& !zr(f,'')){gp(d,b[a],f);}}}}
function ip(b,a){if(zr(a,'Clear')){kp(b);}eq(b.a,a);}
function jp(c){var a,b;a=kl('j1holframe');if(a===null){a=jl();}km(c.a.e,'j1holtabbar');bm(c.a.e,'d7v0');oi(a,c.a.e);oi(a,c.a.a);tf(c);b=null;if(zr(wf(),'Clear')){kp(c);}else{b=lp(c);}if(b!==null&& !zr(b,'')){hp(c,b);op(c);}else{dp(c);}Eo(c);}
function kp(d){var a,b,c;c=yd('j1holtablist');if(c!==null&& !zr(c,'')){b=Er(c,',');for(a=0;a<b.a;a++){if(!zr(b[a],'')){Cd('j1holtab.'+b[a]);}}Cd('j1holtablist');}}
function lp(b){var a;a=yd('j1holtablist');return a;}
function mp(d,c){var a,b;a=yd('j1holtab.'+c);b=Ar(a,124);if(b==(-1)){b=Dr(a);}return cs(a,0,b);}
function np(d,c){var a,b;a=yd('j1holtab.'+c);b=Ar(a,124)+1;if(b==(-1)){b=0;}return bs(a,b);}
function op(a){var b;b=wf();if(Dr(b)>0){ip(a,b);}else{dq(a.a,0);}rp();}
function pp(f,c,a){var b,d,e,g;e=yd('j1holtablist');d=null;if(e===null||zr(e,'')){d=','+c+',';}else if(Br(e,','+c+',')<0){d=e+c+',';}b=aq(f.a,c);g=c;if(b>=0){g=bq(f.a,b);}if(d!==null){Ed('j1holtablist',d);Ed('j1holtab.'+c,g+'|'+a);}}
function rp(){Fo();var f=$doc.getElementsByTagName('span');for(var c=0;c<f.length;c++){var e=f[c];if(e.className=='collapsed'||e.classname=='uncollapsed'){var b=$doc.createElement('div');var a=$doc.createElement('div');var d=e.parentNode;if(e.className=='collapsed'){e.className='xcollapsed';}else{e.className='xuncollapsed';}b.spanElement=e;b.className='collapseboxclosed';b.onclick=function(){if(this.spanElement.className=='xcollapsed'){this.spanElement.className='xuncollapsed';this.className='collapseboxopen';}else if(this.spanElement.className=='xuncollapsed'){this.spanElement.className='xcollapsed';this.className='collapseboxclosed';}};a.className='collapsewidget';b.appendChild(a);d.insertBefore(b,e);}}}
function sp(a){ip(this,a);}
function yn(){}
_=yn.prototype=new or();_.D=sp;_.tI=30;_.b=0;var qp;function An(b,a){b.a=a;return b;}
function Cn(b,a){if(b!=this.a.b){cq(this.a.a,false);this.a.b=b;}}
function zn(){}
_=zn.prototype=new or();_.db=Cn;_.tI=31;function En(b,a,c,d){b.a=a;b.b=c;b.c=d;return b;}
function ao(a,b){fp(this.a);}
function bo(a,b){if(ap(this.a,b)){Ap(this.a.a,'Exercise_'+this.b,jb(b));pp(this.a,'Exercise_'+this.b,this.c);ep(this.a,this.b);}else{fp(this.a);}}
function Dn(){}
_=Dn.prototype=new or();_.C=ao;_.F=bo;_.tI=0;function eo(b,a,c,d){b.a=a;b.b=c;b.c=d;return b;}
function go(a,b){cp(this.a,this.b+1);}
function ho(a,b){if(ap(this.a,b)){Ap(this.a.a,'Solution_'+this.b,jb(b));pp(this.a,'Solution_'+this.b,this.c);}cp(this.a,this.b+1);}
function co(){}
_=co.prototype=new or();_.C=go;_.F=ho;_.tI=0;function jo(b,a,c){b.a=a;b.b=c;return b;}
function lo(a,b){cp(this.a,0);}
function mo(b,c){var a,d;if(ap(this.a,c)){Ap(this.a.a,'Intro',jb(c));pp(this.a,'Intro',this.b);a=qe('j1holtitleid');if(a!==null){d=ve(a);if(d!==null&& !zr(d,'')){Cg(d);}}}cp(this.a,0);}
function io(){}
_=io.prototype=new or();_.C=lo;_.F=mo;_.tI=0;function oo(b,a,c){b.a=a;b.b=c;return b;}
function qo(a,b){op(this.a);bp(this.a,0);}
function ro(a,b){if(ap(this.a,b)){Ap(this.a.a,'Summary',jb(b));pp(this.a,'Summary',this.b);}op(this.a);bp(this.a,0);}
function no(){}
_=no.prototype=new or();_.C=qo;_.F=ro;_.tI=0;function to(b,a,c,d){b.a=a;b.b=c;b.c=d;return b;}
function vo(a,b){}
function wo(a,b){if(ap(this.a,b)){Ap(this.a.a,'Appendix_'+id(rq((Fo(),qp)[this.b])),jb(b));pp(this.a,'Appendix_'+id(rq((Fo(),qp)[this.b])),this.c);}bp(this.a,this.b+1);}
function so(){}
_=so.prototype=new or();_.C=vo;_.F=wo;_.tI=0;function yo(b,a,c){b.a=a;b.b=c;return b;}
function Ao(a,b){}
function Bo(a,b){if(ap(this.a,b)){fq(this.a.a,this.b,jb(b));rp();}}
function xo(){}
_=xo.prototype=new or();_.C=Ao;_.F=Bo;_.tI=0;function xp(a){a.e=wm(new um());a.a=hj(new gj());a.c=Au(new yu());a.d=Au(new yu());}
function yp(b){var a;xp(b);a=fk(new dk());lk(a,(Ej(),Fj));Cu(b.d,a);xm(b.e,a);return b;}
function Ap(c,b,a){Bp(c,b,a,c.c.b);}
function Dp(d,b,e,a){var c;c=a;if(c===null){c='<p class="xxbig j1holwarn centertext">LOADING...<\/p>';}Ep(d,b,e,c,d.c.b);}
function Bp(e,d,a,c){var b,f;b=gq(a);f=jq(b);if(f===null){f=kq(d);}Cp(e,d,f,b,c);}
function Ep(d,c,e,a,b){Cp(d,c,e,gq(a),b);}
function Cp(f,c,g,a,b){var d,e;d=hq(a);e=iq(g,c);zp(f,e);ij(f.a,d);Bu(f.c,b,vp(new up(),c,g,e,d,a,f));if(f.c.b==1){am(e,'selected');mj(f.a,0);}else{fm(e,'selected');}}
function zp(b,a){gk(jd(Fu(b.d,b.d.b-1),15),a);cq(b,true);}
function aq(d,c){var a,b;b=(-1);for(a=0;a<d.c.b;a++){if(zr(jd(Fu(d.c,a),16).b,c)){b=a;break;}else if(as(c,jd(Fu(d.c,a),16).b+'=')){b=a;break;}}return b;}
function bq(b,a){return jd(Fu(b.c,a),16).d;}
function cq(f,c){var a,b,d,e,g;for(b=f.d.b-1;b>=0;b--){a=jd(Fu(f.d,b),15);if(dm(a)>wg()){e=null;if(b>0){e=jd(Fu(f.d,b-1),15);}else if(a.f.b>1){e=fk(new dk());Bu(f.d,0,e);Am(f.e,e,0);b++;}while(a.f.b>1&&dm(a)>wg()){g=aj(a,0);cj(a,0);gk(e,g);}}else if(!c){e=null;d=b-1;if(d>=0){e=jd(Fu(f.d,d),15);}else{break;}while(dm(a)<wg()){if(e.f.b>0){g=aj(e,e.f.b-1);kk(e,g);jk(a,g,0);}else if(d>0){d--;e=jd(Fu(f.d,d),15);}else{break;}}if(dm(a)>wg()){g=aj(a,0);cj(a,0);gk(e,g);}}else{break;}}while(!c){if(jd(Fu(f.d,0),15).f.b==0){cv(f.d,0);cj(f.e,0);}else{break;}}}
function eq(d,b){var a,c;a=aq(d,b);if(a>=0){dq(d,a);c=Ar(b,61);if(c>=1){uf();zf(bs(b,c+1));}}}
function dq(d,b){var a,c;if(d.b!=b){a=jd(Fu(d.c,d.b),16);fm(a.c,'selected');d.b=b;c=jd(Fu(d.c,b),16);am(c.c,'selected');mj(d.a,b);}}
function fq(e,d,a){var b,c;c=aq(e,d);if(c>=0){b=jd(Fu(e.c,c),16);sj(b.a,a);}}
function gq(a){var b;b=qj(new oj(),a);jm(b,'j1holpanel');return b;}
function hq(a){var b,c,d,e;d=ul(new nl());e=ul(new nl());b=ul(new nl());c=ul(new nl());jm(d,'d7');jm(e,'d7v4');jm(b,'cornerBL');jm(c,'cornerBR');wl(c,a);wl(b,c);wl(e,b);wl(d,e);return d;}
function iq(b,d){var a,c;c=ul(new nl());a=pk(new nk(),b,d);jm(c,'j1holtab');wl(c,a);jm(a,'j1holtablink');return c;}
function jq(d){var a,b,c,e;e=null;a=d.i;b=ue(a);while(b!==null){c=pe(b,'name');if(c!==null&&yr(c,'j1holtabname')){e=pe(b,'content');break;}else{b=we(b);}}return e;}
function kq(c){var a,b;b=c;a=(-1);while((a=Ar(b,95))>=0){if(a==0){b=bs(b,1);}else{b=cs(b,0,a)+id(32)+bs(b,a+1);}}return b;}
function tp(){}
_=tp.prototype=new or();_.tI=0;_.b=0;function vp(f,b,g,d,c,a,e){f.b=b;f.d=g;f.c=d;f.a=a;return f;}
function up(){}
_=up.prototype=new or();_.tI=32;_.a=null;_.b=null;_.c=null;_.d=null;function mq(){}
_=mq.prototype=new sr();_.tI=33;function rq(a){return String.fromCharCode(a).toUpperCase().charCodeAt(0);}
function sq(){}
_=sq.prototype=new sr();_.tI=34;function Aq(b,a){tr(b,a);return b;}
function zq(){}
_=zq.prototype=new sr();_.tI=35;function Dq(b,a){tr(b,a);return b;}
function Cq(){}
_=Cq.prototype=new sr();_.tI=36;function ar(b,a){tr(b,a);return b;}
function Fq(){}
_=Fq.prototype=new sr();_.tI=37;function lr(){lr=nx;{nr();}}
function nr(){lr();mr=/^[+-]?\d*\.?\d*(e[+-]?\d+)?$/i;}
var mr=null;function dr(){dr=nx;lr();}
function er(a){dr();return ks(a);}
function fr(){}
_=fr.prototype=new sr();_.tI=38;function ir(b,a){tr(b,a);return b;}
function hr(){}
_=hr.prototype=new sr();_.tI=39;function wr(b,a){return b.charCodeAt(a);}
function zr(b,a){if(!kd(a,1))return false;return gs(b,a);}
function yr(b,a){if(a==null)return false;return b==a||b.toLowerCase()==a.toLowerCase();}
function Ar(b,a){return b.indexOf(String.fromCharCode(a));}
function Br(b,a){return b.indexOf(a);}
function Cr(c,b,a){return c.indexOf(b,a);}
function Dr(a){return a.length;}
function Er(b,a){return Fr(b,a,0);}
function Fr(j,i,g){var a=new RegExp(i,'g');var h=[];var b=0;var k=j;var e=null;while(true){var f=a.exec(k);if(f==null||(k==''||b==g-1&&g>0)){h[b]=k;break;}else{h[b]=k.substring(0,f.index);k=k.substring(f.index+f[0].length,k.length);a.lastIndex=0;if(e==k){h[b]=k.substring(0,1);k=k.substring(1);}e=k;b++;}}if(g==0){for(var c=h.length-1;c>=0;c--){if(h[c]!=''){h.splice(c+1,h.length-(c+1));break;}}}var d=fs(h.length);var c=0;for(c=0;c<h.length;++c){d[c]=h[c];}return d;}
function as(b,a){return Br(b,a)==0;}
function bs(b,a){return b.substr(a,b.length-a);}
function cs(c,a,b){return c.substr(a,b-a);}
function ds(d){var a,b,c;c=Dr(d);a=dd('[C',[0],[(-1)],[c],0);for(b=0;b<c;++b)a[b]=wr(d,b);return a;}
function es(c){var a=c.replace(/^(\s*)/,'');var b=a.replace(/\s*$/,'');return b;}
function fs(a){return dd('[Ljava.lang.String;',[0],[1],[a],null);}
function gs(a,b){return String(a)==b;}
function hs(a){return zr(this,a);}
function js(){var a=is;if(!a){a=is={};}var e=':'+this;var b=a[e];if(b==null){b=0;var f=this.length;var d=f<64?1:f/32|0;for(var c=0;c<f;c+=d){b<<=1;b+=this.charCodeAt(c);}b|=0;a[e]=b;}return b;}
function ks(a){return ''+a;}
_=String.prototype;_.eQ=hs;_.hC=js;_.tI=2;var is=null;function ns(a){return t(a);}
function ts(b,a){tr(b,a);return b;}
function ss(){}
_=ss.prototype=new sr();_.tI=40;function ws(d,a,b){var c;while(a.x()){c=a.A();if(b===null?c===null:b.eQ(c)){return a;}}return null;}
function ys(a){throw ts(new ss(),'add');}
function zs(b){var a;a=ws(this,this.y(),b);return a!==null;}
function vs(){}
_=vs.prototype=new or();_.k=ys;_.m=zs;_.tI=0;function et(b,a){throw ar(new Fq(),'Index: '+a+', Size: '+b.b);}
function ft(a){return Cs(new Bs(),a);}
function gt(b,a){throw ts(new ss(),'add');}
function ht(a){this.j(this.jb(),a);return true;}
function it(e){var a,b,c,d,f;if(e===this){return true;}if(!kd(e,17)){return false;}f=jd(e,17);if(this.jb()!=f.jb()){return false;}c=ft(this);d=f.y();while(Es(c)){a=Fs(c);b=Fs(d);if(!(a===null?b===null:a.eQ(b))){return false;}}return true;}
function jt(){var a,b,c,d;c=1;a=31;b=ft(this);while(Es(b)){d=Fs(b);c=31*c+(d===null?0:d.hC());}return c;}
function kt(){return ft(this);}
function lt(a){throw ts(new ss(),'remove');}
function As(){}
_=As.prototype=new vs();_.j=gt;_.k=ht;_.eQ=it;_.hC=jt;_.y=kt;_.fb=lt;_.tI=41;function Cs(b,a){b.c=a;return b;}
function Es(a){return a.a<a.c.jb();}
function Fs(a){if(!Es(a)){throw new jx();}return a.c.v(a.b=a.a++);}
function at(a){if(a.b<0){throw new Cq();}a.c.fb(a.b);a.a=a.b;a.b=(-1);}
function bt(){return Es(this);}
function ct(){return Fs(this);}
function Bs(){}
_=Bs.prototype=new or();_.x=bt;_.A=ct;_.tI=0;_.a=0;_.b=(-1);function ku(f,d,e){var a,b,c;for(b=gw(f.p());Fv(b);){a=aw(b);c=a.t();if(d===null?c===null:d.eQ(c)){if(e){bw(b);}return a;}}return null;}
function lu(b){var a;a=b.p();return ot(new nt(),b,a);}
function mu(b){var a;a=qw(b);return Ct(new Bt(),b,a);}
function nu(a){return ku(this,a,false)!==null;}
function ou(d){var a,b,c,e,f,g,h;if(d===this){return true;}if(!kd(d,18)){return false;}f=jd(d,18);c=lu(this);e=f.z();if(!vu(c,e)){return false;}for(a=qt(c);xt(a);){b=yt(a);h=this.w(b);g=f.w(b);if(h===null?g!==null:!h.eQ(g)){return false;}}return true;}
function pu(b){var a;a=ku(this,b,false);return a===null?null:a.u();}
function qu(){var a,b,c;b=0;for(c=gw(this.p());Fv(c);){a=aw(c);b+=a.hC();}return b;}
function ru(){return lu(this);}
function su(a,b){throw ts(new ss(),'This map implementation does not support modification');}
function mt(){}
_=mt.prototype=new or();_.l=nu;_.eQ=ou;_.w=pu;_.hC=qu;_.z=ru;_.eb=su;_.tI=42;function vu(e,b){var a,c,d;if(b===e){return true;}if(!kd(b,19)){return false;}c=jd(b,19);if(c.jb()!=e.jb()){return false;}for(a=c.y();a.x();){d=a.A();if(!e.m(d)){return false;}}return true;}
function wu(a){return vu(this,a);}
function xu(){var a,b,c;a=0;for(b=this.y();b.x();){c=b.A();if(c!==null){a+=c.hC();}}return a;}
function tu(){}
_=tu.prototype=new vs();_.eQ=wu;_.hC=xu;_.tI=43;function ot(b,a,c){b.a=a;b.b=c;return b;}
function qt(b){var a;a=gw(b.b);return vt(new ut(),b,a);}
function rt(a){return this.a.l(a);}
function st(){return qt(this);}
function tt(){return this.b.a.c;}
function nt(){}
_=nt.prototype=new tu();_.m=rt;_.y=st;_.jb=tt;_.tI=44;function vt(b,a,c){b.a=c;return b;}
function xt(a){return a.a.x();}
function yt(b){var a;a=b.a.A();return a.t();}
function zt(){return xt(this);}
function At(){return yt(this);}
function ut(){}
_=ut.prototype=new or();_.x=zt;_.A=At;_.tI=0;function Ct(b,a,c){b.a=a;b.b=c;return b;}
function Et(b){var a;a=gw(b.b);return du(new cu(),b,a);}
function Ft(a){return pw(this.a,a);}
function au(){return Et(this);}
function bu(){return this.b.a.c;}
function Bt(){}
_=Bt.prototype=new vs();_.m=Ft;_.y=au;_.jb=bu;_.tI=0;function du(b,a,c){b.a=c;return b;}
function fu(a){return a.a.x();}
function gu(a){var b;b=a.a.A().u();return b;}
function hu(){return fu(this);}
function iu(){return gu(this);}
function cu(){}
_=cu.prototype=new or();_.x=hu;_.A=iu;_.tI=0;function zu(a){{Du(a);}}
function Au(a){zu(a);return a;}
function Bu(c,a,b){if(a<0||a>c.b){et(c,a);}ev(c.a,a,b);++c.b;}
function Cu(b,a){nv(b.a,b.b++,a);return true;}
function Du(a){a.a=E();a.b=0;}
function Fu(b,a){if(a<0||a>=b.b){et(b,a);}return jv(b.a,a);}
function av(b,a){return bv(b,a,0);}
function bv(c,b,a){if(a<0){et(c,a);}for(;a<c.b;++a){if(iv(b,jv(c.a,a))){return a;}}return (-1);}
function cv(c,a){var b;b=Fu(c,a);lv(c.a,a,1);--c.b;return b;}
function dv(c,b){var a;a=av(c,b);if(a==(-1)){return false;}cv(c,a);return true;}
function fv(a,b){Bu(this,a,b);}
function gv(a){return Cu(this,a);}
function ev(a,b,c){a.splice(b,0,c);}
function hv(a){return av(this,a)!=(-1);}
function iv(a,b){return a===b||a!==null&&a.eQ(b);}
function kv(a){return Fu(this,a);}
function jv(a,b){return a[b];}
function mv(a){return cv(this,a);}
function lv(a,c,b){a.splice(c,b);}
function nv(a,b,c){a[b]=c;}
function ov(){return this.b;}
function yu(){}
_=yu.prototype=new As();_.j=fv;_.k=gv;_.m=hv;_.v=kv;_.fb=mv;_.jb=ov;_.tI=45;_.a=null;_.b=0;function nw(){nw=nx;uw=Aw();}
function kw(a){{mw(a);}}
function lw(a){nw();kw(a);return a;}
function mw(a){a.a=E();a.d=ab();a.b=od(uw,A);a.c=0;}
function ow(b,a){if(kd(a,1)){return Ew(b.d,jd(a,1))!==uw;}else if(a===null){return b.b!==uw;}else{return Dw(b.a,a,a.hC())!==uw;}}
function pw(a,b){if(a.b!==uw&&Cw(a.b,b)){return true;}else if(zw(a.d,b)){return true;}else if(xw(a.a,b)){return true;}return false;}
function qw(a){return ew(new Bv(),a);}
function rw(c,a){var b;if(kd(a,1)){b=Ew(c.d,jd(a,1));}else if(a===null){b=c.b;}else{b=Dw(c.a,a,a.hC());}return b===uw?null:b;}
function sw(c,a,d){var b;if(kd(a,1)){b=bx(c.d,jd(a,1),d);}else if(a===null){b=c.b;c.b=d;}else{b=ax(c.a,a,d,a.hC());}if(b===uw){++c.c;return null;}else{return b;}}
function tw(c,a){var b;if(kd(a,1)){b=ex(c.d,jd(a,1));}else if(a===null){b=c.b;c.b=od(uw,A);}else{b=dx(c.a,a,a.hC());}if(b===uw){return null;}else{--c.c;return b;}}
function vw(e,c){nw();for(var d in e){if(d==parseInt(d)){var a=e[d];for(var f=0,b=a.length;f<b;++f){c.k(a[f]);}}}}
function ww(d,a){nw();for(var c in d){if(c.charCodeAt(0)==58){var e=d[c];var b=vv(c.substring(1),e);a.k(b);}}}
function xw(f,h){nw();for(var e in f){if(e==parseInt(e)){var a=f[e];for(var g=0,b=a.length;g<b;++g){var c=a[g];var d=c.u();if(Cw(h,d)){return true;}}}}return false;}
function yw(a){return ow(this,a);}
function zw(c,d){nw();for(var b in c){if(b.charCodeAt(0)==58){var a=c[b];if(Cw(d,a)){return true;}}}return false;}
function Aw(){nw();}
function Bw(){return qw(this);}
function Cw(a,b){nw();if(a===b){return true;}else if(a===null){return false;}else{return a.eQ(b);}}
function Fw(a){return rw(this,a);}
function Dw(f,h,e){nw();var a=f[e];if(a){for(var g=0,b=a.length;g<b;++g){var c=a[g];var d=c.t();if(Cw(h,d)){return c.u();}}}}
function Ew(b,a){nw();return b[':'+a];}
function cx(a,b){return sw(this,a,b);}
function ax(f,h,j,e){nw();var a=f[e];if(a){for(var g=0,b=a.length;g<b;++g){var c=a[g];var d=c.t();if(Cw(h,d)){var i=c.u();c.ib(j);return i;}}}else{a=f[e]=[];}var c=vv(h,j);a.push(c);}
function bx(c,a,d){nw();a=':'+a;var b=c[a];c[a]=d;return b;}
function dx(f,h,e){nw();var a=f[e];if(a){for(var g=0,b=a.length;g<b;++g){var c=a[g];var d=c.t();if(Cw(h,d)){if(a.length==1){delete f[e];}else{a.splice(g,1);}return c.u();}}}}
function ex(c,a){nw();a=':'+a;var b=c[a];delete c[a];return b;}
function rv(){}
_=rv.prototype=new mt();_.l=yw;_.p=Bw;_.w=Fw;_.eb=cx;_.tI=46;_.a=null;_.b=null;_.c=0;_.d=null;var uw;function tv(b,a,c){b.a=a;b.b=c;return b;}
function vv(a,b){return tv(new sv(),a,b);}
function wv(b){var a;if(kd(b,20)){a=jd(b,20);if(Cw(this.a,a.t())&&Cw(this.b,a.u())){return true;}}return false;}
function xv(){return this.a;}
function yv(){return this.b;}
function zv(){var a,b;a=0;b=0;if(this.a!==null){a=this.a.hC();}if(this.b!==null){b=this.b.hC();}return a^b;}
function Av(a){var b;b=this.b;this.b=a;return b;}
function sv(){}
_=sv.prototype=new or();_.eQ=wv;_.t=xv;_.u=yv;_.hC=zv;_.ib=Av;_.tI=47;_.a=null;_.b=null;function ew(b,a){b.a=a;return b;}
function gw(a){return Dv(new Cv(),a.a);}
function hw(c){var a,b,d;if(kd(c,20)){a=jd(c,20);b=a.t();if(ow(this.a,b)){d=rw(this.a,b);return Cw(a.u(),d);}}return false;}
function iw(){return gw(this);}
function jw(){return this.a.c;}
function Bv(){}
_=Bv.prototype=new tu();_.m=hw;_.y=iw;_.jb=jw;_.tI=48;function Dv(c,b){var a;c.c=b;a=Au(new yu());if(c.c.b!==(nw(),uw)){Cu(a,tv(new sv(),null,c.c.b));}ww(c.c.d,a);vw(c.c.a,a);c.a=ft(a);return c;}
function Fv(a){return Es(a.a);}
function aw(a){return a.b=jd(Fs(a.a),20);}
function bw(a){if(a.b===null){throw Dq(new Cq(),'Must call next() before remove().');}else{at(a.a);tw(a.c,a.b.t());a.b=null;}}
function cw(){return Fv(this);}
function dw(){return aw(this);}
function Cv(){}
_=Cv.prototype=new or();_.x=cw;_.A=dw;_.tI=0;_.a=null;_.b=null;function jx(){}
_=jx.prototype=new sr();_.tI=49;function lq(){jp(Do(new yn()));}
function gwtOnLoad(b,d,c){$moduleName=d;$moduleBase=c;if(b)try{lq();}catch(a){b(d);}else{lq();}}
var nd=[{},{},{1:1},{3:1},{3:1},{3:1},{3:1},{2:1},{6:1},{6:1},{3:1,14:1},{3:1,14:1},{3:1,14:1},{2:1,4:1},{2:1},{7:1},{9:1,11:1,12:1,13:1},{9:1,11:1,12:1,13:1},{9:1,11:1,12:1,13:1},{9:1,11:1,12:1,13:1},{9:1,11:1,12:1,13:1},{9:1,11:1,12:1,13:1},{9:1,11:1,12:1,13:1},{9:1,11:1,12:1,13:1},{9:1,11:1,12:1,13:1,15:1},{9:1,11:1,12:1,13:1},{9:1,10:1,11:1,12:1,13:1},{7:1},{9:1,11:1,12:1,13:1},{9:1,11:1,12:1,13:1},{5:1},{8:1},{16:1},{3:1},{3:1},{3:1},{3:1},{3:1},{3:1},{3:1},{3:1},{17:1},{18:1},{19:1},{19:1},{17:1},{18:1},{20:1},{19:1},{3:1}];if (com_sun_javaone_HoLTemplate) {  var __gwt_initHandlers = com_sun_javaone_HoLTemplate.__gwt_initHandlers;  com_sun_javaone_HoLTemplate.onScriptLoad(gwtOnLoad);}})();