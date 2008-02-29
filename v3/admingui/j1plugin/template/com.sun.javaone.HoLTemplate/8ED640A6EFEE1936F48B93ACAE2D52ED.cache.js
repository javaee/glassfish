(function(){var $wnd = window;var $doc = $wnd.document;var $moduleName, $moduleBase;var _,wx='com.google.gwt.core.client.',xx='com.google.gwt.http.client.',yx='com.google.gwt.lang.',zx='com.google.gwt.user.client.',Ax='com.google.gwt.user.client.impl.',Bx='com.google.gwt.user.client.ui.',Cx='com.sun.javaone.client.',Dx='java.lang.',Ex='java.util.';function vx(){}
function yr(a){return this===a;}
function zr(){return vs(this);}
function wr(){}
_=wr.prototype={};_.eQ=yr;_.hC=zr;_.tI=1;function o(){return u();}
var p=null;function s(a){return a==null?0:a.$H?a.$H:(a.$H=v());}
function t(a){return a==null?0:a.$H?a.$H:(a.$H=v());}
function u(){var b=$doc.location.href;var a=b.indexOf('#');if(a!= -1)b=b.substring(0,a);a=b.indexOf('?');if(a!= -1)b=b.substring(0,a);a=b.lastIndexOf('/');if(a!= -1)b=b.substring(0,a);return b.length>0?b+'/':'';}
function v(){return ++w;}
var w=0;function xs(b,a){a;return b;}
function zs(b,a){if(b.a!==null){throw fr(new er(),"Can't overwrite cause");}if(a===b){throw cr(new br(),'Self-causation not permitted');}b.a=a;return b;}
function ws(){}
_=ws.prototype=new wr();_.tI=3;_.a=null;function Fq(b,a){xs(b,a);return b;}
function Eq(){}
_=Eq.prototype=new ws();_.tI=4;function Br(b,a){Fq(b,a);return b;}
function Ar(){}
_=Ar.prototype=new Eq();_.tI=5;function y(c,b,a){Br(c,'JavaScript '+b+' exception: '+a);return c;}
function x(){}
_=x.prototype=new Ar();_.tI=6;function C(b,a){if(!kd(a,2)){return false;}return bb(b,jd(a,2));}
function D(a){return s(a);}
function E(){return [];}
function F(){return function(){};}
function ab(){return {};}
function cb(a){return C(this,a);}
function bb(a,b){return a===b;}
function db(){return D(this);}
function A(){}
_=A.prototype=new wr();_.eQ=cb;_.hC=db;_.tI=7;function ec(b,d,c,a){if(d===null){throw new pr();}if(a===null){throw new pr();}if(c<0){throw new br();}b.a=c;b.c=d;if(c>0){b.b=lb(new kb(),b,a);eg(b.b,c);}else{b.b=null;}return b;}
function gc(a){var b;if(a.c!==null){b=a.c;a.c=null;vc(b);fc(a);}}
function fc(a){if(a.b!==null){bg(a.b);}}
function ic(e,a){var b,c,d,f;if(e.c===null){return;}fc(e);f=e.c;e.c=null;b=wc(f);if(b!==null){c=Br(new Ar(),b);a.C(e,c);}else{d=kc(f);a.F(e,d);}}
function jc(b,a){if(b.c===null){return;}gc(b);a.C(b,bc(new ac(),b,b.a));}
function kc(b){var a;a=gb(new fb(),b);return a;}
function lc(a){var b;b=p;{ic(this,a);}}
function eb(){}
_=eb.prototype=new wr();_.q=lc;_.tI=0;_.a=0;_.b=null;_.c=null;function mc(){}
_=mc.prototype=new wr();_.tI=0;function gb(a,b){a.a=b;return a;}
function ib(a){return yc(a.a);}
function jb(a){return xc(a.a);}
function fb(){}
_=fb.prototype=new mc();_.tI=0;function cg(){cg=vx;kg=cv(new av());{jg();}}
function ag(a){cg();return a;}
function bg(a){if(a.c){fg(a.d);}else{gg(a.d);}lv(kg,a);}
function dg(a){if(!a.c){lv(kg,a);}a.hb();}
function eg(b,a){if(a<=0){throw cr(new br(),'must be positive');}bg(b);b.c=false;b.d=hg(b,a);ev(kg,b);}
function fg(a){cg();$wnd.clearInterval(a);}
function gg(a){cg();$wnd.clearTimeout(a);}
function hg(b,a){cg();return $wnd.setTimeout(function(){b.r();},a);}
function ig(){var a;a=p;{dg(this);}}
function jg(){cg();pg(new Cf());}
function Bf(){}
_=Bf.prototype=new wr();_.r=ig;_.tI=8;_.c=false;_.d=0;var kg;function mb(){mb=vx;cg();}
function lb(b,a,c){mb();b.a=a;b.b=c;ag(b);return b;}
function nb(){jc(this.a,this.b);}
function kb(){}
_=kb.prototype=new Bf();_.hb=nb;_.tI=9;function ub(){ub=vx;xb=qb(new pb(),'GET');qb(new pb(),'POST');yb=fi(new ei());}
function sb(b,a,c){ub();tb(b,a===null?null:a.a,c);return b;}
function tb(b,a,c){ub();qc('httpMethod',a);qc('url',c);b.a=a;b.c=c;return b;}
function vb(g,d,a){var b,c,e,f,h;h=hi(yb);{b=zc(h,g.a,g.c,true);}if(b!==null){e=Eb(new Db(),g.c);zs(e,Bb(new Ab(),b));throw e;}wb(g,h);c=ec(new eb(),h,g.b,a);f=Ac(h,c,d,a);if(f!==null){throw Bb(new Ab(),f);}return c;}
function wb(a,b){{Bc(b,'Content-Type','text/plain; charset=utf-8');}}
function ob(){}
_=ob.prototype=new wr();_.tI=0;_.a=null;_.b=0;_.c=null;var xb,yb;function qb(b,a){b.a=a;return b;}
function pb(){}
_=pb.prototype=new wr();_.tI=0;_.a=null;function Bb(b,a){Fq(b,a);return b;}
function Ab(){}
_=Ab.prototype=new Eq();_.tI=10;function Eb(a,b){Bb(a,'The URL '+b+' is invalid or violates the same-origin security restriction');return a;}
function Db(){}
_=Db.prototype=new Ab();_.tI=11;function bc(b,a,c){Bb(b,dc(c));return b;}
function dc(a){return 'A request timeout has expired after '+mr(a)+' ms';}
function ac(){}
_=ac.prototype=new Ab();_.tI=12;function qc(a,b){rc(a,b);if(0==fs(ms(b))){throw cr(new br(),a+' can not be empty');}}
function rc(a,b){if(null===b){throw qr(new pr(),a+' can not be null');}}
function vc(a){a.onreadystatechange=ji;a.abort();}
function wc(b){try{if(b.status===undefined){return 'XmlHttpRequest.status == undefined, please see Safari bug '+'http://bugs.webkit.org/show_bug.cgi?id=3810 for more details';}return null;}catch(a){return 'Unable to read XmlHttpRequest.status; likely causes are a '+'networking error or bad cross-domain request. Please see '+'https://bugzilla.mozilla.org/show_bug.cgi?id=238559 for more '+'details';}}
function xc(a){return a.responseText;}
function yc(a){return a.status;}
function zc(e,c,d,b){try{e.open(c,d,b);return null;}catch(a){return a.message||a.toString();}}
function Ac(e,c,d,b){e.onreadystatechange=function(){if(e.readyState==uc){e.onreadystatechange=ji;c.q(b);}};try{e.send(d);return null;}catch(a){e.onreadystatechange=ji;return a.message||a.toString();}}
function Bc(d,b,c){try{d.setRequestHeader(b,c);return null;}catch(a){return a.message||a.toString();}}
var uc=4;function Dc(c,a,d,b,e){c.a=a;c.b=b;e;c.tI=d;return c;}
function Fc(a,b,c){return a[b]=c;}
function ad(b,a){return b[a];}
function bd(a){return a.length;}
function dd(e,d,c,b,a){return cd(e,d,c,b,0,bd(b),a);}
function cd(j,i,g,c,e,a,b){var d,f,h;if((f=ad(c,e))<0){throw new nr();}h=Dc(new Cc(),f,ad(i,e),ad(g,e),j);++e;if(e<a){j=js(j,1);for(d=0;d<f;++d){Fc(h,d,cd(j,i,g,c,e,a,b));}}else{for(d=0;d<f;++d){Fc(h,d,b);}}return h;}
function ed(a,b,c){if(c!==null&&a.b!=0&& !kd(c,a.b)){throw new uq();}return Fc(a,b,c);}
function Cc(){}
_=Cc.prototype=new wr();_.tI=0;function hd(b,a){return !(!(b&&nd[b][a]));}
function id(a){return String.fromCharCode(a);}
function jd(b,a){if(b!=null)hd(b.tI,a)||md();return b;}
function kd(b,a){return b!=null&&hd(b.tI,a);}
function md(){throw new Aq();}
function ld(a){if(a!==null){throw new Aq();}return a;}
function od(b,d){_=d.prototype;if(b&& !(b.tI>=_.tI)){var c=b.toString;for(var a in _){b[a]=_[a];}b.toString=c;}return b;}
var nd;function rd(a){if(kd(a,3)){return a;}return y(new x(),td(a),sd(a));}
function sd(a){return a.message;}
function td(a){return a.name;}
function xd(){if(wd===null||Ad()){wd=tw(new zv());zd(wd);}return wd;}
function yd(b){var a;a=xd();return jd(zw(a,b),1);}
function zd(e){var b=$doc.cookie;if(b&&b!=''){var a=b.split('; ');for(var d=0;d<a.length;++d){var f,g;var c=a[d].indexOf('=');if(c== -1){f=a[d];g='';}else{f=a[d].substring(0,c);g=a[d].substring(c+1);}f=decodeURIComponent(f);g=decodeURIComponent(g);e.eb(f,g);}}}
function Ad(){var a=$doc.cookie;if(a!=''&&a!=Bd){Bd=a;return true;}else{return false;}}
function Cd(a){$doc.cookie=a+"='';expires='Fri, 02-Jan-1970 00:00:00 GMT'";}
function Ed(a,b){Dd(a,b,0,null,null,false);}
function Dd(d,g,c,b,e,f){var a=encodeURIComponent(d)+'='+encodeURIComponent(g);if(c)a+=';expires='+new Date(c).toGMTString();if(b)a+=';domain='+b;if(e)a+=';path='+e;if(f)a+=';secure';$doc.cookie=a;}
var wd=null,Bd=null;function ae(){ae=vx;De=cv(new av());{ye=new Fg();eh(ye);}}
function be(b,a){ae();qh(ye,b,a);}
function ce(a,b){ae();return ch(ye,a,b);}
function de(){ae();return sh(ye,'A');}
function ee(){ae();return sh(ye,'div');}
function fe(){ae();return sh(ye,'tbody');}
function ge(){ae();return sh(ye,'td');}
function he(){ae();return sh(ye,'tr');}
function ie(){ae();return sh(ye,'table');}
function le(b,a,d){ae();var c;c=p;{ke(b,a,d);}}
function ke(b,a,c){ae();var d;if(a===Ce){if(ne(b)==8192){Ce=null;}}d=je;je=b;try{c.B(b);}finally{je=d;}}
function me(b,a){ae();th(ye,b,a);}
function ne(a){ae();return uh(ye,a);}
function oe(a){ae();jh(ye,a);}
function pe(b,a){ae();return vh(ye,b,a);}
function qe(a){ae();return wh(ye,a);}
function se(a,b){ae();return yh(ye,a,b);}
function re(a,b){ae();return xh(ye,a,b);}
function te(a){ae();return zh(ye,a);}
function ue(a){ae();return kh(ye,a);}
function ve(a){ae();return Ah(ye,a);}
function we(a){ae();return lh(ye,a);}
function xe(a){ae();return mh(ye,a);}
function ze(c,a,b){ae();oh(ye,c,a,b);}
function Ae(a){ae();var b,c;c=true;if(De.b>0){b=ld(hv(De,De.b-1));if(!(c=null.lb())){me(a,true);oe(a);}}return c;}
function Be(b,a){ae();Bh(ye,b,a);}
function Ee(a,b,c){ae();Ch(ye,a,b,c);}
function Fe(a,b){ae();Dh(ye,a,b);}
function af(a,b){ae();Eh(ye,a,b);}
function bf(a,b){ae();Fh(ye,a,b);}
function cf(b,a,c){ae();ai(ye,b,a,c);}
function df(a,b){ae();gh(ye,a,b);}
function ef(){ae();return bi(ye);}
function ff(){ae();return ci(ye);}
var je=null,ye=null,Ce=null,De;function jf(a){if(kd(a,4)){return ce(this,jd(a,4));}return C(od(this,gf),a);}
function kf(){return D(od(this,gf));}
function gf(){}
_=gf.prototype=new A();_.eQ=jf;_.hC=kf;_.tI=13;function of(a){return C(od(this,lf),a);}
function pf(){return D(od(this,lf));}
function lf(){}
_=lf.prototype=new A();_.eQ=of;_.hC=pf;_.tI=14;function sf(){sf=vx;xf=cv(new av());{yf=new li();if(!qi(yf)){yf=null;}}}
function tf(a){sf();ev(xf,a);}
function uf(){sf();$wnd.history.back();}
function vf(a){sf();var b,c;for(b=nt(xf);gt(b);){c=jd(ht(b),5);c.D(a);}}
function wf(){sf();return yf!==null?si(yf):'';}
function zf(a){sf();if(yf!==null){ni(yf,a);}}
function Af(b){sf();var a;a=p;{vf(b);}}
var xf,yf=null;function Ef(){while((cg(),kg).b>0){bg(jd(hv((cg(),kg),0),6));}}
function Ff(){return null;}
function Cf(){}
_=Cf.prototype=new wr();_.bb=Ef;_.cb=Ff;_.tI=15;function og(){og=vx;rg=cv(new av());Bg=cv(new av());{xg();}}
function pg(a){og();ev(rg,a);}
function qg(a){og();ev(Bg,a);}
function sg(){og();var a,b;for(a=nt(rg);gt(a);){b=jd(ht(a),7);b.bb();}}
function tg(){og();var a,b,c,d;d=null;for(a=nt(rg);gt(a);){b=jd(ht(a),7);c=b.cb();{d=c;}}return d;}
function ug(){og();var a,b;for(a=nt(Bg);gt(a);){b=jd(ht(a),8);b.db(wg(),vg());}}
function vg(){og();return ef();}
function wg(){og();return ff();}
function xg(){og();__gwt_initHandlers(function(){Ag();},function(){return zg();},function(){yg();$wnd.onresize=null;$wnd.onbeforeclose=null;$wnd.onclose=null;});}
function yg(){og();var a;a=p;{sg();}}
function zg(){og();var a;a=p;{return tg();}}
function Ag(){og();var a;a=p;{ug();}}
function Cg(a){og();$doc.title=a;}
var rg,Bg;function qh(c,b,a){b.appendChild(a);}
function sh(b,a){return $doc.createElement(a);}
function th(c,b,a){b.cancelBubble=a;}
function uh(b,a){switch(a.type){case 'blur':return 4096;case 'change':return 1024;case 'click':return 1;case 'dblclick':return 2;case 'focus':return 2048;case 'keydown':return 128;case 'keypress':return 256;case 'keyup':return 512;case 'load':return 32768;case 'losecapture':return 8192;case 'mousedown':return 4;case 'mousemove':return 64;case 'mouseout':return 32;case 'mouseover':return 16;case 'mouseup':return 8;case 'scroll':return 16384;case 'error':return 65536;case 'mousewheel':return 131072;case 'DOMMouseScroll':return 131072;}}
function vh(d,b,a){var c=b.getAttribute(a);return c==null?null:c;}
function wh(c,b){var a=$doc.getElementById(b);return a||null;}
function yh(d,a,b){var c=a[b];return c==null?null:String(c);}
function xh(d,a,c){var b=parseInt(a[c]);if(!b){return 0;}return b;}
function zh(b,a){return a.__eventBits||0;}
function Ah(d,b){var c='',a=b.firstChild;while(a){if(a.nodeType==1){c+=d.s(a);}else if(a.nodeValue){c+=a.nodeValue;}a=a.nextSibling;}return c;}
function Bh(c,b,a){b.removeChild(a);}
function Ch(c,a,b,d){a[b]=d;}
function Dh(c,a,b){a.__listener=b;}
function Eh(c,a,b){if(!b){b='';}a.innerHTML=b;}
function Fh(c,a,b){while(a.firstChild){a.removeChild(a.firstChild);}if(b!=null){a.appendChild($doc.createTextNode(b));}}
function ai(c,b,a,d){b.style[a]=d;}
function bi(a){return $doc.body.clientHeight;}
function ci(a){return $doc.body.clientWidth;}
function di(a){return Ah(this,a);}
function Dg(){}
_=Dg.prototype=new wr();_.s=di;_.tI=0;function jh(b,a){a.preventDefault();}
function kh(c,b){var a=b.firstChild;while(a&&a.nodeType!=1)a=a.nextSibling;return a||null;}
function lh(c,a){var b=a.nextSibling;while(b&&b.nodeType!=1)b=b.nextSibling;return b||null;}
function mh(c,a){var b=a.parentNode;if(b==null){return null;}if(b.nodeType!=1)b=null;return b||null;}
function nh(d){$wnd.__dispatchCapturedMouseEvent=function(b){if($wnd.__dispatchCapturedEvent(b)){var a=$wnd.__captureElem;if(a&&a.__listener){le(b,a,a.__listener);b.stopPropagation();}}};$wnd.__dispatchCapturedEvent=function(a){if(!Ae(a)){a.stopPropagation();a.preventDefault();return false;}return true;};$wnd.addEventListener('click',$wnd.__dispatchCapturedMouseEvent,true);$wnd.addEventListener('dblclick',$wnd.__dispatchCapturedMouseEvent,true);$wnd.addEventListener('mousedown',$wnd.__dispatchCapturedMouseEvent,true);$wnd.addEventListener('mouseup',$wnd.__dispatchCapturedMouseEvent,true);$wnd.addEventListener('mousemove',$wnd.__dispatchCapturedMouseEvent,true);$wnd.addEventListener('mousewheel',$wnd.__dispatchCapturedMouseEvent,true);$wnd.addEventListener('keydown',$wnd.__dispatchCapturedEvent,true);$wnd.addEventListener('keyup',$wnd.__dispatchCapturedEvent,true);$wnd.addEventListener('keypress',$wnd.__dispatchCapturedEvent,true);$wnd.__dispatchEvent=function(b){var c,a=this;while(a&& !(c=a.__listener))a=a.parentNode;if(a&&a.nodeType!=1)a=null;if(c)le(b,a,c);};$wnd.__captureElem=null;}
function oh(f,e,g,d){var c=0,b=e.firstChild,a=null;while(b){if(b.nodeType==1){if(c==d){a=b;break;}++c;}b=b.nextSibling;}e.insertBefore(g,a);}
function ph(c,b,a){b.__eventBits=a;b.onclick=a&1?$wnd.__dispatchEvent:null;b.ondblclick=a&2?$wnd.__dispatchEvent:null;b.onmousedown=a&4?$wnd.__dispatchEvent:null;b.onmouseup=a&8?$wnd.__dispatchEvent:null;b.onmouseover=a&16?$wnd.__dispatchEvent:null;b.onmouseout=a&32?$wnd.__dispatchEvent:null;b.onmousemove=a&64?$wnd.__dispatchEvent:null;b.onkeydown=a&128?$wnd.__dispatchEvent:null;b.onkeypress=a&256?$wnd.__dispatchEvent:null;b.onkeyup=a&512?$wnd.__dispatchEvent:null;b.onchange=a&1024?$wnd.__dispatchEvent:null;b.onfocus=a&2048?$wnd.__dispatchEvent:null;b.onblur=a&4096?$wnd.__dispatchEvent:null;b.onlosecapture=a&8192?$wnd.__dispatchEvent:null;b.onscroll=a&16384?$wnd.__dispatchEvent:null;b.onload=a&32768?$wnd.__dispatchEvent:null;b.onerror=a&65536?$wnd.__dispatchEvent:null;b.onmousewheel=a&131072?$wnd.__dispatchEvent:null;}
function hh(){}
_=hh.prototype=new Dg();_.tI=0;function ch(c,a,b){if(!a&& !b){return true;}else if(!a|| !b){return false;}return a.isSameNode(b);}
function eh(a){nh(a);dh(a);}
function dh(d){$wnd.addEventListener('mouseout',function(b){var a=$wnd.__captureElem;if(a&& !b.relatedTarget){if('html'==b.target.tagName.toLowerCase()){var c=$doc.createEvent('MouseEvents');c.initMouseEvent('mouseup',true,true,$wnd,0,b.screenX,b.screenY,b.clientX,b.clientY,b.ctrlKey,b.altKey,b.shiftKey,b.metaKey,b.button,null);a.dispatchEvent(c);}}},true);$wnd.addEventListener('DOMMouseScroll',$wnd.__dispatchCapturedMouseEvent,true);}
function gh(c,b,a){ph(c,b,a);fh(c,b,a);}
function fh(c,b,a){if(a&131072){b.addEventListener('DOMMouseScroll',$wnd.__dispatchEvent,false);}}
function Eg(){}
_=Eg.prototype=new hh();_.tI=0;function Fg(){}
_=Fg.prototype=new Eg();_.tI=0;function fi(a){ji=F();return a;}
function hi(a){return ii(a);}
function ii(a){return new XMLHttpRequest();}
function ei(){}
_=ei.prototype=new wr();_.tI=0;var ji=null;function si(a){return $wnd.__gwt_historyToken;}
function ti(a){Af(a);}
function ki(){}
_=ki.prototype=new wr();_.tI=0;function qi(d){$wnd.__gwt_historyToken='';var c=$wnd.location.hash;if(c.length>0)$wnd.__gwt_historyToken=c.substring(1);$wnd.__checkHistory=function(){var b='',a=$wnd.location.hash;if(a.length>0)b=a.substring(1);if(b!=$wnd.__gwt_historyToken){$wnd.__gwt_historyToken=b;ti(b);}$wnd.setTimeout('__checkHistory()',250);};$wnd.__checkHistory();return true;}
function oi(){}
_=oi.prototype=new ki();_.tI=0;function ni(d,a){if(a==null||a.length==0){var c=$wnd.location.href;var b=c.indexOf('#');if(b!= -1)c=c.substring(0,b);$wnd.location=c+'#';}else{$wnd.location.hash=encodeURIComponent(a);}}
function li(){}
_=li.prototype=new oi();_.tI=0;function im(b,a){jm(b,mm(b)+id(45)+a);}
function jm(b,a){ym(b.i,a,true);}
function lm(a){return re(a.i,'offsetWidth');}
function mm(a){return wm(a.i);}
function nm(b,a){om(b,mm(b)+id(45)+a);}
function om(b,a){ym(b.i,a,false);}
function pm(d,b,a){var c=b.parentNode;if(!c){return;}c.insertBefore(a,b);c.removeChild(b);}
function qm(b,a){if(b.i!==null){pm(b,b.i,a);}b.i=a;}
function rm(b,a){xm(b.i,a);}
function sm(b,a){zm(b.i,a);}
function tm(a,b){Am(a.i,b);}
function um(b,a){df(b.i,a|te(b.i));}
function vm(a){return se(a,'className');}
function wm(a){var b,c;b=vm(a);c=cs(b,32);if(c>=0){return ks(b,0,c);}return b;}
function xm(a,b){Ee(a,'className',b);}
function ym(c,j,a){var b,d,e,f,g,h,i;if(c===null){throw Br(new Ar(),'Null widget handle. If you are creating a composite, ensure that initWidget() has been called.');}j=ms(j);if(fs(j)==0){throw cr(new br(),'Style names cannot be empty');}i=vm(c);e=ds(i,j);while(e!=(-1)){if(e==0||Er(i,e-1)==32){f=e+fs(j);g=fs(i);if(f==g||f<g&&Er(i,f)==32){break;}}e=es(i,j,e+1);}if(a){if(e==(-1)){if(fs(i)>0){i+=' ';}Ee(c,'className',i+j);}}else{if(e!=(-1)){b=ms(ks(i,0,e));d=ms(js(i,e+fs(j)));if(fs(b)==0){h=d;}else if(fs(d)==0){h=b;}else{h=b+' '+d;}Ee(c,'className',h);}}}
function zm(a,b){if(a===null){throw Br(new Ar(),'Null widget handle. If you are creating a composite, ensure that initWidget() has been called.');}b=ms(b);if(fs(b)==0){throw cr(new br(),'Style names cannot be empty');}Bm(a,b);}
function Am(a,b){a.style.display=b?'':'none';}
function Bm(b,f){var a=b.className.split(/\s+/);if(!a){return;}var g=a[0];var h=g.length;a[0]=f;for(var c=1,d=a.length;c<d;c++){var e=a[c];if(e.length>h&&(e.charAt(h)=='-'&&e.indexOf(g)==0)){a[c]=f+e.substring(h);}}b.className=a.join(' ');}
function hm(){}
_=hm.prototype=new wr();_.tI=0;_.i=null;function wn(a){if(a.g){throw fr(new er(),"Should only call onAttach when the widget is detached from the browser's document");}a.g=true;Fe(a.i,a);a.n();a.E();}
function xn(a){if(!a.g){throw fr(new er(),"Should only call onDetach when the widget is attached to the browser's document");}try{a.ab();}finally{a.o();Fe(a.i,null);a.g=false;}}
function yn(a){if(a.h!==null){a.h.gb(a);}else if(a.h!==null){throw fr(new er(),"This widget's parent does not implement HasWidgets");}}
function zn(b,a){if(b.g){Fe(b.i,null);}qm(b,a);if(b.g){Fe(a,b);}}
function An(c,b){var a;a=c.h;if(b===null){if(a!==null&&a.g){xn(c);}c.h=null;}else{if(a!==null){throw fr(new er(),'Cannot set a new parent without first clearing the old parent');}c.h=b;if(b.g){wn(c);}}}
function Bn(){}
function Cn(){}
function Dn(a){}
function En(){}
function Fn(){}
function en(){}
_=en.prototype=new hm();_.n=Bn;_.o=Cn;_.B=Dn;_.E=En;_.ab=Fn;_.tI=16;_.g=false;_.h=null;function cl(b,a){An(a,b);}
function el(b,a){An(a,null);}
function fl(){var a,b;for(b=this.y();b.x();){a=jd(b.A(),9);wn(a);}}
function gl(){var a,b;for(b=this.y();b.x();){a=jd(b.A(),9);xn(a);}}
function hl(){}
function il(){}
function bl(){}
_=bl.prototype=new en();_.n=fl;_.o=gl;_.E=hl;_.ab=il;_.tI=17;function aj(a){a.f=mn(new fn(),a);}
function bj(a){aj(a);return a;}
function cj(c,a,b){yn(a);nn(c.f,a);be(b,a.i);cl(c,a);}
function dj(d,b,a){var c;fj(d,a);if(b.h===d){c=hj(d,b);if(c<a){a--;}}return a;}
function ej(b,a){if(a<0||a>=b.f.b){throw new hr();}}
function fj(b,a){if(a<0||a>b.f.b){throw new hr();}}
function ij(b,a){return pn(b.f,a);}
function hj(b,a){return qn(b.f,a);}
function jj(e,b,c,a,d){a=dj(e,b,a);yn(b);rn(e.f,b,a);if(d){ze(c,b.i,a);}else{be(c,b.i);}cl(e,b);}
function kj(b,a){return b.gb(ij(b,a));}
function lj(b,c){var a;if(c.h!==b){return false;}el(b,c);a=c.i;Be(xe(a),a);un(b.f,c);return true;}
function mj(){return sn(this.f);}
function nj(a){return lj(this,a);}
function Fi(){}
_=Fi.prototype=new bl();_.y=mj;_.gb=nj;_.tI=18;function vi(a){bj(a);zn(a,ee());cf(a.i,'position','relative');cf(a.i,'overflow','hidden');return a;}
function wi(a,b){cj(a,b,a.i);}
function yi(a){cf(a,'left','');cf(a,'top','');cf(a,'position','');}
function zi(b){var a;a=lj(this,b);if(a){yi(b.i);}return a;}
function ui(){}
_=ui.prototype=new Fi();_.gb=zi;_.tI=19;function Bi(a){bj(a);a.e=ie();a.d=fe();be(a.e,a.d);zn(a,a.e);return a;}
function Di(c,b,a){Ee(b,'align',a.a);}
function Ei(c,b,a){cf(b,'verticalAlign',a.a);}
function Ai(){}
_=Ai.prototype=new Fi();_.tI=20;_.d=null;_.e=null;function pj(a){bj(a);zn(a,ee());return a;}
function qj(a,b){cj(a,b,a.i);sj(a,b);}
function sj(b,c){var a;a=c.i;cf(a,'width','100%');cf(a,'height','100%');tm(c,false);}
function tj(a,b){cf(b.i,'width','');cf(b.i,'height','');tm(b,true);}
function uj(b,a){ej(b,a);if(b.a!==null){tm(b.a,false);}b.a=ij(b,a);tm(b.a,true);}
function vj(b){var a;a=lj(this,b);if(a){tj(this,b);if(this.a===b){this.a=null;}}return a;}
function oj(){}
_=oj.prototype=new Fi();_.gb=vj;_.tI=21;_.a=null;function Ek(a){zn(a,ee());um(a,131197);rm(a,'gwt-Label');return a;}
function al(a){switch(ne(a)){case 1:break;case 4:case 8:case 64:case 16:case 32:break;case 131072:break;}}
function Dk(){}
_=Dk.prototype=new en();_.B=al;_.tI=22;function xj(a){Ek(a);zn(a,ee());um(a,125);rm(a,'gwt-HTML');return a;}
function yj(b,a){xj(b);Aj(b,a);return b;}
function Aj(b,a){af(b.i,a);}
function wj(){}
_=wj.prototype=new Dk();_.tI=23;function ak(){ak=vx;Ej(new Dj(),'center');bk=Ej(new Dj(),'left');Ej(new Dj(),'right');}
var bk;function Ej(b,a){b.a=a;return b;}
function Dj(){}
_=Dj.prototype=new wr();_.tI=0;_.a=null;function gk(){gk=vx;hk=ek(new dk(),'bottom');ek(new dk(),'middle');ik=ek(new dk(),'top');}
var hk,ik;function ek(a,b){a.a=b;return a;}
function dk(){}
_=dk.prototype=new wr();_.tI=0;_.a=null;function mk(a){a.a=(ak(),bk);a.c=(gk(),ik);}
function nk(a){Bi(a);mk(a);a.b=he();be(a.d,a.b);Ee(a.e,'cellSpacing','0');Ee(a.e,'cellPadding','0');return a;}
function ok(b,c){var a;a=qk(b);be(b.b,a);cj(b,c,a);}
function qk(b){var a;a=ge();Di(b,a,b.a);Ei(b,a,b.c);return a;}
function rk(c,d,a){var b;fj(c,a);b=qk(c);ze(c.b,b,a);jj(c,d,b,a,false);}
function sk(c,d){var a,b;b=xe(d.i);a=lj(c,d);if(a){Be(c.b,b);}return a;}
function tk(b,a){b.c=a;}
function uk(a){return sk(this,a);}
function lk(){}
_=lk.prototype=new Ai();_.gb=uk;_.tI=24;_.b=null;function wk(a){zn(a,ee());be(a.i,a.a=de());um(a,1);rm(a,'gwt-Hyperlink');return a;}
function xk(c,b,a){wk(c);Ak(c,b);zk(c,a);return c;}
function zk(b,a){b.b=a;Ee(b.a,'href','#'+a);}
function Ak(b,a){bf(b.a,a);}
function Bk(a){if(ne(a)==1){zf(this.b);oe(a);}}
function vk(){}
_=vk.prototype=new en();_.B=Bk;_.tI=25;_.a=null;_.b=null;function pl(){pl=vx;ul=tw(new zv());}
function ol(b,a){pl();vi(b);if(a===null){a=ql();}zn(b,a);wn(b);return b;}
function rl(){pl();return sl(null);}
function sl(c){pl();var a,b;b=jd(zw(ul,c),10);if(b!==null){return b;}a=null;if(c!==null){if(null===(a=qe(c))){return null;}}if(ul.c==0){tl();}Aw(ul,c,b=ol(new jl(),a));return b;}
function ql(){pl();return $doc.body;}
function tl(){pl();pg(new kl());}
function jl(){}
_=jl.prototype=new ui();_.tI=26;var ul;function ml(){var a,b;for(b=gu(uu((pl(),ul)));nu(b);){a=jd(ou(b),10);if(a.g){xn(a);}}}
function nl(){return null;}
function kl(){}
_=kl.prototype=new wr();_.bb=ml;_.cb=nl;_.tI=27;function Cl(a){Dl(a,ee());return a;}
function Dl(b,a){zn(b,a);return b;}
function El(a,b){if(a.a!==null){throw fr(new er(),'SimplePanel can only contain one child widget');}bm(a,b);}
function am(a,b){if(a.a!==b){return false;}el(a,b);Be(a.i,b.i);a.a=null;return true;}
function bm(a,b){if(b===a.a){return;}if(b!==null){yn(b);}if(a.a!==null){am(a,a.a);}a.a=b;if(b!==null){be(a.i,a.a.i);cl(a,b);}}
function cm(){return yl(new wl(),this);}
function dm(a){return am(this,a);}
function vl(){}
_=vl.prototype=new bl();_.y=cm;_.gb=dm;_.tI=28;_.a=null;function xl(a){a.a=a.b.a!==null;}
function yl(b,a){b.b=a;xl(b);return b;}
function Al(){return this.a;}
function Bl(){if(!this.a||this.b.a===null){throw new rx();}this.a=false;return this.b.a;}
function wl(){}
_=wl.prototype=new wr();_.x=Al;_.A=Bl;_.tI=0;function Dm(a){a.a=(ak(),bk);a.b=(gk(),ik);}
function Em(a){Bi(a);Dm(a);Ee(a.e,'cellSpacing','0');Ee(a.e,'cellPadding','0');return a;}
function Fm(b,d){var a,c;c=he();a=bn(b);be(c,a);be(b.d,c);cj(b,d,a);}
function bn(b){var a;a=ge();Di(b,a,b.a);Ei(b,a,b.b);return a;}
function cn(c,e,a){var b,d;fj(c,a);d=he();b=bn(c);be(d,b);ze(c.d,d,a);jj(c,e,b,a,false);}
function dn(c){var a,b;b=xe(c.i);a=lj(this,c);if(a){Be(this.d,xe(b));}return a;}
function Cm(){}
_=Cm.prototype=new Ai();_.gb=dn;_.tI=29;function mn(b,a){b.a=dd('[Lcom.google.gwt.user.client.ui.Widget;',[0],[9],[4],null);return b;}
function nn(a,b){rn(a,b,a.b);}
function pn(b,a){if(a<0||a>=b.b){throw new hr();}return b.a[a];}
function qn(b,c){var a;for(a=0;a<b.b;++a){if(b.a[a]===c){return a;}}return (-1);}
function rn(d,e,a){var b,c;if(a<0||a>d.b){throw new hr();}if(d.b==d.a.a){c=dd('[Lcom.google.gwt.user.client.ui.Widget;',[0],[9],[d.a.a*2],null);for(b=0;b<d.a.a;++b){ed(c,b,d.a[b]);}d.a=c;}++d.b;for(b=d.b-1;b>a;--b){ed(d.a,b,d.a[b-1]);}ed(d.a,a,e);}
function sn(a){return hn(new gn(),a);}
function tn(c,b){var a;if(b<0||b>=c.b){throw new hr();}--c.b;for(a=b;a<c.b;++a){ed(c.a,a,c.a[a+1]);}ed(c.a,c.b,null);}
function un(b,c){var a;a=qn(b,c);if(a==(-1)){throw new rx();}tn(b,a);}
function fn(){}
_=fn.prototype=new wr();_.tI=0;_.a=null;_.b=0;function hn(b,a){b.b=a;return b;}
function kn(){return this.a<this.b.b-1;}
function ln(){if(this.a>=this.b.b){throw new rx();}return this.b.a[++this.a];}
function gn(){}
_=gn.prototype=new wr();_.x=kn;_.A=ln;_.tI=0;_.a=(-1);function hp(){hp=vx;yp=ls('abcdefghijklmnopqrstuvwxyz');}
function ep(a){a.a=aq(new Bp());}
function fp(a){hp();ep(a);return a;}
function gp(a){qg(co(new bo(),a));}
function ip(c,a){var b;b=jb(a);return (ib(a)==200||ib(a)==203||ib(a)<100)&&b!==null&& !bs(b,'');}
function jp(e,d){var a,c,f;f=o()+'/appendix'+id(yp[d])+'.html';c=sb(new ob(),(ub(),xb),f);try{vb(c,null,Bo(new Ao(),e,d,f));}catch(a){a=rd(a);if(kd(a,14)){}else throw a;}}
function kp(e,d){var a,c,f;f=o()+'/exercise'+d+'.html';c=sb(new ob(),(ub(),xb),f);try{vb(c,null,ho(new go(),e,d,f));}catch(a){a=rd(a);if(kd(a,14)){a;np(e);}else throw a;}}
function lp(d){var a,c,e;e=o()+'/intro.html';c=sb(new ob(),(ub(),xb),e);try{vb(c,null,ro(new qo(),d,e));}catch(a){a=rd(a);if(kd(a,14)){a;kp(d,0);}else throw a;}}
function mp(e,d){var a,c,f;f=o()+'/solution'+d+'.html';c=sb(new ob(),(ub(),xb),f);try{vb(c,null,mo(new lo(),e,d,f));}catch(a){a=rd(a);if(kd(a,14)){a;kp(e,d+1);}else throw a;}}
function np(d){var a,c,e;e=o()+'/summary.html';c=sb(new ob(),(ub(),xb),e);try{vb(c,null,wo(new vo(),d,e));}catch(a){a=rd(a);if(kd(a,14)){a;wp(d);jp(d,0);}else throw a;}}
function op(e,d,f){var a,c;c=sb(new ob(),(ub(),xb),f);try{vb(c,null,ap(new Fo(),e,d));}catch(a){a=rd(a);if(kd(a,14)){}else throw a;}}
function pp(d,c){var a,b,e,f;b=gs(c,',');for(a=0;a<b.a;a++){if(!bs(b[a],'')){e=up(d,b[a]);f=vp(d,b[a]);fq(d.a,b[a],e,null);if(f!==null&& !bs(f,'')){op(d,b[a],f);}}}}
function qp(b,a){if(bs(a,'Clear')){sp(b);}mq(b.a,a);}
function rp(c){var a,b;a=sl('j1holframe');if(a===null){a=rl();}sm(c.a.e,'j1holtabbar');jm(c.a.e,'d7v0');wi(a,c.a.e);wi(a,c.a.a);tf(c);b=null;if(bs(wf(),'Clear')){sp(c);}else{b=tp(c);}if(b!==null&& !bs(b,'')){pp(c,b);wp(c);}else{lp(c);}gp(c);}
function sp(d){var a,b,c;c=yd('j1holtablist');if(c!==null&& !bs(c,'')){b=gs(c,',');for(a=0;a<b.a;a++){if(!bs(b[a],'')){Cd('j1holtab.'+b[a]);}}Cd('j1holtablist');}}
function tp(b){var a;a=yd('j1holtablist');return a;}
function up(d,c){var a,b;a=yd('j1holtab.'+c);b=cs(a,124);if(b==(-1)){b=fs(a);}return ks(a,0,b);}
function vp(d,c){var a,b;a=yd('j1holtab.'+c);b=cs(a,124)+1;if(b==(-1)){b=0;}return js(a,b);}
function wp(a){var b;b=wf();if(fs(b)>0){qp(a,b);}else{lq(a.a,0);}zp();}
function xp(f,c,a){var b,d,e,g;e=yd('j1holtablist');d=null;if(e===null||bs(e,'')){d=','+c+',';}else if(ds(e,','+c+',')<0){d=e+c+',';}b=iq(f.a,c);g=c;if(b>=0){g=jq(f.a,b);}if(d!==null){Ed('j1holtablist',d);Ed('j1holtab.'+c,g+'|'+a);}}
function zp(){hp();var f=$doc.getElementsByTagName('span');for(var c=0;c<f.length;c++){var e=f[c];if(e.className=='collapsed'||e.classname=='uncollapsed'){var b=$doc.createElement('div');var a=$doc.createElement('div');var d=e.parentNode;if(e.className=='collapsed'){e.className='xcollapsed';}else{e.className='xuncollapsed';}b.spanElement=e;b.className='collapseboxclosed';b.onclick=function(){if(this.spanElement.className=='xcollapsed'){this.spanElement.className='xuncollapsed';this.className='collapseboxopen';}else if(this.spanElement.className=='xuncollapsed'){this.spanElement.className='xcollapsed';this.className='collapseboxclosed';}};a.className='collapsewidget';b.appendChild(a);d.insertBefore(b,e);}}}
function Ap(a){qp(this,a);}
function ao(){}
_=ao.prototype=new wr();_.D=Ap;_.tI=30;_.b=0;var yp;function co(b,a){b.a=a;return b;}
function fo(b,a){if(b!=this.a.b){kq(this.a.a,false);this.a.b=b;}}
function bo(){}
_=bo.prototype=new wr();_.db=fo;_.tI=31;function ho(b,a,c,d){b.a=a;b.b=c;b.c=d;return b;}
function jo(a,b){np(this.a);}
function ko(a,b){if(ip(this.a,b)){cq(this.a.a,'Exercise_'+this.b,jb(b));xp(this.a,'Exercise_'+this.b,this.c);mp(this.a,this.b);}else{np(this.a);}}
function go(){}
_=go.prototype=new wr();_.C=jo;_.F=ko;_.tI=0;function mo(b,a,c,d){b.a=a;b.b=c;b.c=d;return b;}
function oo(a,b){kp(this.a,this.b+1);}
function po(a,b){if(ip(this.a,b)){cq(this.a.a,'Solution_'+this.b,jb(b));xp(this.a,'Solution_'+this.b,this.c);}kp(this.a,this.b+1);}
function lo(){}
_=lo.prototype=new wr();_.C=oo;_.F=po;_.tI=0;function ro(b,a,c){b.a=a;b.b=c;return b;}
function to(a,b){kp(this.a,0);}
function uo(b,c){var a,d;if(ip(this.a,c)){cq(this.a.a,'Intro',jb(c));xp(this.a,'Intro',this.b);a=qe('j1holtitleid');if(a!==null){d=ve(a);if(d!==null&& !bs(d,'')){Cg(d);}}}kp(this.a,0);}
function qo(){}
_=qo.prototype=new wr();_.C=to;_.F=uo;_.tI=0;function wo(b,a,c){b.a=a;b.b=c;return b;}
function yo(a,b){wp(this.a);jp(this.a,0);}
function zo(a,b){if(ip(this.a,b)){cq(this.a.a,'Summary',jb(b));xp(this.a,'Summary',this.b);}wp(this.a);jp(this.a,0);}
function vo(){}
_=vo.prototype=new wr();_.C=yo;_.F=zo;_.tI=0;function Bo(b,a,c,d){b.a=a;b.b=c;b.c=d;return b;}
function Do(a,b){}
function Eo(a,b){if(ip(this.a,b)){cq(this.a.a,'Appendix_'+id(zq((hp(),yp)[this.b])),jb(b));xp(this.a,'Appendix_'+id(zq((hp(),yp)[this.b])),this.c);}jp(this.a,this.b+1);}
function Ao(){}
_=Ao.prototype=new wr();_.C=Do;_.F=Eo;_.tI=0;function ap(b,a,c){b.a=a;b.b=c;return b;}
function cp(a,b){}
function dp(a,b){if(ip(this.a,b)){nq(this.a.a,this.b,jb(b));zp();}}
function Fo(){}
_=Fo.prototype=new wr();_.C=cp;_.F=dp;_.tI=0;function Fp(a){a.e=Em(new Cm());a.a=pj(new oj());a.c=cv(new av());a.d=cv(new av());}
function aq(b){var a;Fp(b);a=nk(new lk());tk(a,(gk(),hk));ev(b.d,a);Fm(b.e,a);return b;}
function cq(c,b,a){dq(c,b,a,c.c.b);}
function fq(d,b,e,a){var c;c=a;if(c===null){c='<p class="xxbig j1holwarn centertext">LOADING...<\/p>';}gq(d,b,e,c,d.c.b);}
function dq(e,d,a,c){var b,f;b=oq(a);f=rq(b);if(f===null){f=sq(d);}eq(e,d,f,b,c);}
function gq(d,c,e,a,b){eq(d,c,e,oq(a),b);}
function eq(f,c,g,a,b){var d,e;d=pq(a);e=qq(g,c);bq(f,e);qj(f.a,d);dv(f.c,b,Dp(new Cp(),c,g,e,d,a,f));if(f.c.b==1){im(e,'selected');uj(f.a,0);}else{nm(e,'selected');}}
function bq(b,a){ok(jd(hv(b.d,b.d.b-1),15),a);kq(b,true);}
function iq(d,c){var a,b;b=(-1);for(a=0;a<d.c.b;a++){if(bs(jd(hv(d.c,a),16).b,c)){b=a;break;}else if(is(c,jd(hv(d.c,a),16).b+'=')){b=a;break;}}return b;}
function jq(b,a){return jd(hv(b.c,a),16).d;}
function kq(f,c){var a,b,d,e,g;for(b=f.d.b-1;b>=0;b--){a=jd(hv(f.d,b),15);if(lm(a)>wg()){e=null;if(b>0){e=jd(hv(f.d,b-1),15);}else if(a.f.b>1){e=nk(new lk());dv(f.d,0,e);cn(f.e,e,0);b++;}while(a.f.b>1&&lm(a)>wg()){g=ij(a,0);kj(a,0);ok(e,g);}}else if(!c){e=null;d=b-1;if(d>=0){e=jd(hv(f.d,d),15);}else{break;}while(lm(a)<wg()){if(e.f.b>0){g=ij(e,e.f.b-1);sk(e,g);rk(a,g,0);}else if(d>0){d--;e=jd(hv(f.d,d),15);}else{break;}}if(lm(a)>wg()){g=ij(a,0);kj(a,0);ok(e,g);}}else{break;}}while(!c){if(jd(hv(f.d,0),15).f.b==0){kv(f.d,0);kj(f.e,0);}else{break;}}}
function mq(d,b){var a,c;a=iq(d,b);if(a>=0){lq(d,a);c=cs(b,61);if(c>=1){uf();zf(js(b,c+1));}}}
function lq(d,b){var a,c;if(d.b!=b){a=jd(hv(d.c,d.b),16);nm(a.c,'selected');d.b=b;c=jd(hv(d.c,b),16);im(c.c,'selected');uj(d.a,b);}}
function nq(e,d,a){var b,c;c=iq(e,d);if(c>=0){b=jd(hv(e.c,c),16);Aj(b.a,a);}}
function oq(a){var b;b=yj(new wj(),a);rm(b,'j1holpanel');return b;}
function pq(a){var b,c,d,e;d=Cl(new vl());e=Cl(new vl());b=Cl(new vl());c=Cl(new vl());rm(d,'d7');rm(e,'d7v4');rm(b,'cornerBL');rm(c,'cornerBR');El(c,a);El(b,c);El(e,b);El(d,e);return d;}
function qq(b,d){var a,c;c=Cl(new vl());a=xk(new vk(),b,d);rm(c,'j1holtab');El(c,a);rm(a,'j1holtablink');return c;}
function rq(d){var a,b,c,e;e=null;a=d.i;b=ue(a);while(b!==null){c=pe(b,'name');if(c!==null&&as(c,'j1holtabname')){e=pe(b,'content');break;}else{b=we(b);}}return e;}
function sq(c){var a,b;b=c;a=(-1);while((a=cs(b,95))>=0){if(a==0){b=js(b,1);}else{b=ks(b,0,a)+id(32)+js(b,a+1);}}return b;}
function Bp(){}
_=Bp.prototype=new wr();_.tI=0;_.b=0;function Dp(f,b,g,d,c,a,e){f.b=b;f.d=g;f.c=d;f.a=a;return f;}
function Cp(){}
_=Cp.prototype=new wr();_.tI=32;_.a=null;_.b=null;_.c=null;_.d=null;function uq(){}
_=uq.prototype=new Ar();_.tI=33;function zq(a){return String.fromCharCode(a).toUpperCase().charCodeAt(0);}
function Aq(){}
_=Aq.prototype=new Ar();_.tI=34;function cr(b,a){Br(b,a);return b;}
function br(){}
_=br.prototype=new Ar();_.tI=35;function fr(b,a){Br(b,a);return b;}
function er(){}
_=er.prototype=new Ar();_.tI=36;function ir(b,a){Br(b,a);return b;}
function hr(){}
_=hr.prototype=new Ar();_.tI=37;function tr(){tr=vx;{vr();}}
function vr(){tr();ur=/^[+-]?\d*\.?\d*(e[+-]?\d+)?$/i;}
var ur=null;function lr(){lr=vx;tr();}
function mr(a){lr();return ss(a);}
function nr(){}
_=nr.prototype=new Ar();_.tI=38;function qr(b,a){Br(b,a);return b;}
function pr(){}
_=pr.prototype=new Ar();_.tI=39;function Er(b,a){return b.charCodeAt(a);}
function bs(b,a){if(!kd(a,1))return false;return os(b,a);}
function as(b,a){if(a==null)return false;return b==a||b.toLowerCase()==a.toLowerCase();}
function cs(b,a){return b.indexOf(String.fromCharCode(a));}
function ds(b,a){return b.indexOf(a);}
function es(c,b,a){return c.indexOf(b,a);}
function fs(a){return a.length;}
function gs(b,a){return hs(b,a,0);}
function hs(j,i,g){var a=new RegExp(i,'g');var h=[];var b=0;var k=j;var e=null;while(true){var f=a.exec(k);if(f==null||(k==''||b==g-1&&g>0)){h[b]=k;break;}else{h[b]=k.substring(0,f.index);k=k.substring(f.index+f[0].length,k.length);a.lastIndex=0;if(e==k){h[b]=k.substring(0,1);k=k.substring(1);}e=k;b++;}}if(g==0){for(var c=h.length-1;c>=0;c--){if(h[c]!=''){h.splice(c+1,h.length-(c+1));break;}}}var d=ns(h.length);var c=0;for(c=0;c<h.length;++c){d[c]=h[c];}return d;}
function is(b,a){return ds(b,a)==0;}
function js(b,a){return b.substr(a,b.length-a);}
function ks(c,a,b){return c.substr(a,b-a);}
function ls(d){var a,b,c;c=fs(d);a=dd('[C',[0],[(-1)],[c],0);for(b=0;b<c;++b)a[b]=Er(d,b);return a;}
function ms(c){var a=c.replace(/^(\s*)/,'');var b=a.replace(/\s*$/,'');return b;}
function ns(a){return dd('[Ljava.lang.String;',[0],[1],[a],null);}
function os(a,b){return String(a)==b;}
function ps(a){return bs(this,a);}
function rs(){var a=qs;if(!a){a=qs={};}var e=':'+this;var b=a[e];if(b==null){b=0;var f=this.length;var d=f<64?1:f/32|0;for(var c=0;c<f;c+=d){b<<=1;b+=this.charCodeAt(c);}b|=0;a[e]=b;}return b;}
function ss(a){return ''+a;}
_=String.prototype;_.eQ=ps;_.hC=rs;_.tI=2;var qs=null;function vs(a){return t(a);}
function Bs(b,a){Br(b,a);return b;}
function As(){}
_=As.prototype=new Ar();_.tI=40;function Es(d,a,b){var c;while(a.x()){c=a.A();if(b===null?c===null:b.eQ(c)){return a;}}return null;}
function at(a){throw Bs(new As(),'add');}
function bt(b){var a;a=Es(this,this.y(),b);return a!==null;}
function Ds(){}
_=Ds.prototype=new wr();_.k=at;_.m=bt;_.tI=0;function mt(b,a){throw ir(new hr(),'Index: '+a+', Size: '+b.b);}
function nt(a){return et(new dt(),a);}
function ot(b,a){throw Bs(new As(),'add');}
function pt(a){this.j(this.jb(),a);return true;}
function qt(e){var a,b,c,d,f;if(e===this){return true;}if(!kd(e,17)){return false;}f=jd(e,17);if(this.jb()!=f.jb()){return false;}c=nt(this);d=f.y();while(gt(c)){a=ht(c);b=ht(d);if(!(a===null?b===null:a.eQ(b))){return false;}}return true;}
function rt(){var a,b,c,d;c=1;a=31;b=nt(this);while(gt(b)){d=ht(b);c=31*c+(d===null?0:d.hC());}return c;}
function st(){return nt(this);}
function tt(a){throw Bs(new As(),'remove');}
function ct(){}
_=ct.prototype=new Ds();_.j=ot;_.k=pt;_.eQ=qt;_.hC=rt;_.y=st;_.fb=tt;_.tI=41;function et(b,a){b.c=a;return b;}
function gt(a){return a.a<a.c.jb();}
function ht(a){if(!gt(a)){throw new rx();}return a.c.v(a.b=a.a++);}
function it(a){if(a.b<0){throw new er();}a.c.fb(a.b);a.a=a.b;a.b=(-1);}
function jt(){return gt(this);}
function kt(){return ht(this);}
function dt(){}
_=dt.prototype=new wr();_.x=jt;_.A=kt;_.tI=0;_.a=0;_.b=(-1);function su(f,d,e){var a,b,c;for(b=ow(f.p());hw(b);){a=iw(b);c=a.t();if(d===null?c===null:d.eQ(c)){if(e){jw(b);}return a;}}return null;}
function tu(b){var a;a=b.p();return wt(new vt(),b,a);}
function uu(b){var a;a=yw(b);return eu(new du(),b,a);}
function vu(a){return su(this,a,false)!==null;}
function wu(d){var a,b,c,e,f,g,h;if(d===this){return true;}if(!kd(d,18)){return false;}f=jd(d,18);c=tu(this);e=f.z();if(!Du(c,e)){return false;}for(a=yt(c);Ft(a);){b=au(a);h=this.w(b);g=f.w(b);if(h===null?g!==null:!h.eQ(g)){return false;}}return true;}
function xu(b){var a;a=su(this,b,false);return a===null?null:a.u();}
function yu(){var a,b,c;b=0;for(c=ow(this.p());hw(c);){a=iw(c);b+=a.hC();}return b;}
function zu(){return tu(this);}
function Au(a,b){throw Bs(new As(),'This map implementation does not support modification');}
function ut(){}
_=ut.prototype=new wr();_.l=vu;_.eQ=wu;_.w=xu;_.hC=yu;_.z=zu;_.eb=Au;_.tI=42;function Du(e,b){var a,c,d;if(b===e){return true;}if(!kd(b,19)){return false;}c=jd(b,19);if(c.jb()!=e.jb()){return false;}for(a=c.y();a.x();){d=a.A();if(!e.m(d)){return false;}}return true;}
function Eu(a){return Du(this,a);}
function Fu(){var a,b,c;a=0;for(b=this.y();b.x();){c=b.A();if(c!==null){a+=c.hC();}}return a;}
function Bu(){}
_=Bu.prototype=new Ds();_.eQ=Eu;_.hC=Fu;_.tI=43;function wt(b,a,c){b.a=a;b.b=c;return b;}
function yt(b){var a;a=ow(b.b);return Dt(new Ct(),b,a);}
function zt(a){return this.a.l(a);}
function At(){return yt(this);}
function Bt(){return this.b.a.c;}
function vt(){}
_=vt.prototype=new Bu();_.m=zt;_.y=At;_.jb=Bt;_.tI=44;function Dt(b,a,c){b.a=c;return b;}
function Ft(a){return a.a.x();}
function au(b){var a;a=b.a.A();return a.t();}
function bu(){return Ft(this);}
function cu(){return au(this);}
function Ct(){}
_=Ct.prototype=new wr();_.x=bu;_.A=cu;_.tI=0;function eu(b,a,c){b.a=a;b.b=c;return b;}
function gu(b){var a;a=ow(b.b);return lu(new ku(),b,a);}
function hu(a){return xw(this.a,a);}
function iu(){return gu(this);}
function ju(){return this.b.a.c;}
function du(){}
_=du.prototype=new Ds();_.m=hu;_.y=iu;_.jb=ju;_.tI=0;function lu(b,a,c){b.a=c;return b;}
function nu(a){return a.a.x();}
function ou(a){var b;b=a.a.A().u();return b;}
function pu(){return nu(this);}
function qu(){return ou(this);}
function ku(){}
_=ku.prototype=new wr();_.x=pu;_.A=qu;_.tI=0;function bv(a){{fv(a);}}
function cv(a){bv(a);return a;}
function dv(c,a,b){if(a<0||a>c.b){mt(c,a);}mv(c.a,a,b);++c.b;}
function ev(b,a){vv(b.a,b.b++,a);return true;}
function fv(a){a.a=E();a.b=0;}
function hv(b,a){if(a<0||a>=b.b){mt(b,a);}return rv(b.a,a);}
function iv(b,a){return jv(b,a,0);}
function jv(c,b,a){if(a<0){mt(c,a);}for(;a<c.b;++a){if(qv(b,rv(c.a,a))){return a;}}return (-1);}
function kv(c,a){var b;b=hv(c,a);tv(c.a,a,1);--c.b;return b;}
function lv(c,b){var a;a=iv(c,b);if(a==(-1)){return false;}kv(c,a);return true;}
function nv(a,b){dv(this,a,b);}
function ov(a){return ev(this,a);}
function mv(a,b,c){a.splice(b,0,c);}
function pv(a){return iv(this,a)!=(-1);}
function qv(a,b){return a===b||a!==null&&a.eQ(b);}
function sv(a){return hv(this,a);}
function rv(a,b){return a[b];}
function uv(a){return kv(this,a);}
function tv(a,c,b){a.splice(c,b);}
function vv(a,b,c){a[b]=c;}
function wv(){return this.b;}
function av(){}
_=av.prototype=new ct();_.j=nv;_.k=ov;_.m=pv;_.v=sv;_.fb=uv;_.jb=wv;_.tI=45;_.a=null;_.b=0;function vw(){vw=vx;Cw=cx();}
function sw(a){{uw(a);}}
function tw(a){vw();sw(a);return a;}
function uw(a){a.a=E();a.d=ab();a.b=od(Cw,A);a.c=0;}
function ww(b,a){if(kd(a,1)){return gx(b.d,jd(a,1))!==Cw;}else if(a===null){return b.b!==Cw;}else{return fx(b.a,a,a.hC())!==Cw;}}
function xw(a,b){if(a.b!==Cw&&ex(a.b,b)){return true;}else if(bx(a.d,b)){return true;}else if(Fw(a.a,b)){return true;}return false;}
function yw(a){return mw(new dw(),a);}
function zw(c,a){var b;if(kd(a,1)){b=gx(c.d,jd(a,1));}else if(a===null){b=c.b;}else{b=fx(c.a,a,a.hC());}return b===Cw?null:b;}
function Aw(c,a,d){var b;if(kd(a,1)){b=jx(c.d,jd(a,1),d);}else if(a===null){b=c.b;c.b=d;}else{b=ix(c.a,a,d,a.hC());}if(b===Cw){++c.c;return null;}else{return b;}}
function Bw(c,a){var b;if(kd(a,1)){b=mx(c.d,jd(a,1));}else if(a===null){b=c.b;c.b=od(Cw,A);}else{b=lx(c.a,a,a.hC());}if(b===Cw){return null;}else{--c.c;return b;}}
function Dw(e,c){vw();for(var d in e){if(d==parseInt(d)){var a=e[d];for(var f=0,b=a.length;f<b;++f){c.k(a[f]);}}}}
function Ew(d,a){vw();for(var c in d){if(c.charCodeAt(0)==58){var e=d[c];var b=Dv(c.substring(1),e);a.k(b);}}}
function Fw(f,h){vw();for(var e in f){if(e==parseInt(e)){var a=f[e];for(var g=0,b=a.length;g<b;++g){var c=a[g];var d=c.u();if(ex(h,d)){return true;}}}}return false;}
function ax(a){return ww(this,a);}
function bx(c,d){vw();for(var b in c){if(b.charCodeAt(0)==58){var a=c[b];if(ex(d,a)){return true;}}}return false;}
function cx(){vw();}
function dx(){return yw(this);}
function ex(a,b){vw();if(a===b){return true;}else if(a===null){return false;}else{return a.eQ(b);}}
function hx(a){return zw(this,a);}
function fx(f,h,e){vw();var a=f[e];if(a){for(var g=0,b=a.length;g<b;++g){var c=a[g];var d=c.t();if(ex(h,d)){return c.u();}}}}
function gx(b,a){vw();return b[':'+a];}
function kx(a,b){return Aw(this,a,b);}
function ix(f,h,j,e){vw();var a=f[e];if(a){for(var g=0,b=a.length;g<b;++g){var c=a[g];var d=c.t();if(ex(h,d)){var i=c.u();c.ib(j);return i;}}}else{a=f[e]=[];}var c=Dv(h,j);a.push(c);}
function jx(c,a,d){vw();a=':'+a;var b=c[a];c[a]=d;return b;}
function lx(f,h,e){vw();var a=f[e];if(a){for(var g=0,b=a.length;g<b;++g){var c=a[g];var d=c.t();if(ex(h,d)){if(a.length==1){delete f[e];}else{a.splice(g,1);}return c.u();}}}}
function mx(c,a){vw();a=':'+a;var b=c[a];delete c[a];return b;}
function zv(){}
_=zv.prototype=new ut();_.l=ax;_.p=dx;_.w=hx;_.eb=kx;_.tI=46;_.a=null;_.b=null;_.c=0;_.d=null;var Cw;function Bv(b,a,c){b.a=a;b.b=c;return b;}
function Dv(a,b){return Bv(new Av(),a,b);}
function Ev(b){var a;if(kd(b,20)){a=jd(b,20);if(ex(this.a,a.t())&&ex(this.b,a.u())){return true;}}return false;}
function Fv(){return this.a;}
function aw(){return this.b;}
function bw(){var a,b;a=0;b=0;if(this.a!==null){a=this.a.hC();}if(this.b!==null){b=this.b.hC();}return a^b;}
function cw(a){var b;b=this.b;this.b=a;return b;}
function Av(){}
_=Av.prototype=new wr();_.eQ=Ev;_.t=Fv;_.u=aw;_.hC=bw;_.ib=cw;_.tI=47;_.a=null;_.b=null;function mw(b,a){b.a=a;return b;}
function ow(a){return fw(new ew(),a.a);}
function pw(c){var a,b,d;if(kd(c,20)){a=jd(c,20);b=a.t();if(ww(this.a,b)){d=zw(this.a,b);return ex(a.u(),d);}}return false;}
function qw(){return ow(this);}
function rw(){return this.a.c;}
function dw(){}
_=dw.prototype=new Bu();_.m=pw;_.y=qw;_.jb=rw;_.tI=48;function fw(c,b){var a;c.c=b;a=cv(new av());if(c.c.b!==(vw(),Cw)){ev(a,Bv(new Av(),null,c.c.b));}Ew(c.c.d,a);Dw(c.c.a,a);c.a=nt(a);return c;}
function hw(a){return gt(a.a);}
function iw(a){return a.b=jd(ht(a.a),20);}
function jw(a){if(a.b===null){throw fr(new er(),'Must call next() before remove().');}else{it(a.a);Bw(a.c,a.b.t());a.b=null;}}
function kw(){return hw(this);}
function lw(){return iw(this);}
function ew(){}
_=ew.prototype=new wr();_.x=kw;_.A=lw;_.tI=0;_.a=null;_.b=null;function rx(){}
_=rx.prototype=new Ar();_.tI=49;function tq(){rp(fp(new ao()));}
function gwtOnLoad(b,d,c){$moduleName=d;$moduleBase=c;if(b)try{tq();}catch(a){b(d);}else{tq();}}
var nd=[{},{},{1:1},{3:1},{3:1},{3:1},{3:1},{2:1},{6:1},{6:1},{3:1,14:1},{3:1,14:1},{3:1,14:1},{2:1,4:1},{2:1},{7:1},{9:1,11:1,12:1,13:1},{9:1,11:1,12:1,13:1},{9:1,11:1,12:1,13:1},{9:1,11:1,12:1,13:1},{9:1,11:1,12:1,13:1},{9:1,11:1,12:1,13:1},{9:1,11:1,12:1,13:1},{9:1,11:1,12:1,13:1},{9:1,11:1,12:1,13:1,15:1},{9:1,11:1,12:1,13:1},{9:1,10:1,11:1,12:1,13:1},{7:1},{9:1,11:1,12:1,13:1},{9:1,11:1,12:1,13:1},{5:1},{8:1},{16:1},{3:1},{3:1},{3:1},{3:1},{3:1},{3:1},{3:1},{3:1},{17:1},{18:1},{19:1},{19:1},{17:1},{18:1},{20:1},{19:1},{3:1}];if (com_sun_javaone_HoLTemplate) {  var __gwt_initHandlers = com_sun_javaone_HoLTemplate.__gwt_initHandlers;  com_sun_javaone_HoLTemplate.onScriptLoad(gwtOnLoad);}})();