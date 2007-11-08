/*
	Copyright (c) 2004-2006, The Dojo Foundation
	All Rights Reserved.

	Licensed under the Academic Free License version 2.1 or above OR the
	modified BSD license. For more information on Dojo licensing, see:

		http://dojotoolkit.org/community/licensing.shtml
*/

/*
	This is a compiled version of Dojo, built for deployment and not for
	development. To get an editable version, please visit:

		http://dojotoolkit.org

	for documentation and information on getting the source.
*/

if(typeof dojo=="undefined"){
var dj_global=this;
var dj_currentContext=this;
function dj_undef(_1,_2){
return (typeof (_2||dj_currentContext)[_1]=="undefined");
}
if(dj_undef("djConfig",this)){
var djConfig={};
}
if(dj_undef("dojo",this)){
var dojo={};
}
dojo.global=function(){
return dj_currentContext;
};
dojo.locale=djConfig.locale;
dojo.version={major:0,minor:0,patch:0,flag:"dev",revision:Number("$Rev: 7460 $".match(/[0-9]+/)[0]),toString:function(){
with(dojo.version){
return major+"."+minor+"."+patch+flag+" ("+revision+")";
}
}};
dojo.getObject=function(_3,_4,_5,_6){
var _7,_8;
if(typeof _3!="string"){
return undefined;
}
_7=_5;
if(!_7){
_7=dojo.global();
}
var _9=_3.split("."),i=0,_b,_c,_d;
do{
_b=_7;
_d=_9[i];
_c=_7[_9[i]];
if((_4)&&(!_c)){
_c=_7[_9[i]]={};
}
_7=_c;
i++;
}while(i<_9.length&&_7);
_8=_7;
_7=_b;
return (_6)?{obj:_7,prop:_d}:_8;
};
dojo.exists=function(_e,_f){
if(typeof _f=="string"){
dojo.deprecated("dojo.exists(obj, name)","use dojo.exists(name, obj, /*optional*/create)","0.6");
var tmp=_e;
_e=_f;
_f=tmp;
}
return (!!dojo.getObject(_e,false,_f));
};
dojo.evalProp=function(_11,_12,_13){
dojo.deprecated("dojo.evalProp","just use hash syntax. Sheesh.","0.6");
return _12[_11]||(_13?(_12[_11]={}):undefined);
};
dojo.parseObjPath=function(_14,_15,_16){
dojo.deprecated("dojo.parseObjPath","use dojo.getObject(path, create, context, true)","0.6");
return dojo.getObject(_14,_16,_15,true);
};
dojo.evalObjPath=function(_17,_18){
dojo.deprecated("dojo.evalObjPath","use dojo.getObject(path, create)","0.6");
return dojo.getObject(_17,_18);
};
dojo.errorToString=function(_19){
return (_19["message"]||_19["description"]||_19);
};
dojo.raise=function(_1a,_1b){
if(_1b){
_1a=_1a+": "+dojo.errorToString(_1b);
}else{
_1a=dojo.errorToString(_1a);
}
try{
if(djConfig.isDebug){
dojo.hostenv.println("FATAL exception raised: "+_1a);
}
}
catch(e){
}
throw _1b||Error(_1a);
};
dojo.debug=function(){
};
dojo.debugShallow=function(obj){
};
dojo.profile={start:function(){
},end:function(){
},stop:function(){
},dump:function(){
}};
function dj_eval(_1d){
return dj_global.eval?dj_global.eval(_1d):eval(_1d);
}
dojo.unimplemented=function(_1e,_1f){
var _20="'"+_1e+"' not implemented";
if(_1f!=null){
_20+=" "+_1f;
}
dojo.raise(_20);
};
dojo.deprecated=function(_21,_22,_23){
var _24="DEPRECATED: "+_21;
if(_22){
_24+=" "+_22;
}
if(_23){
_24+=" -- will be removed in version: "+_23;
}
dojo.debug(_24);
};
dojo.render=(function(){
function vscaffold(_25,_26){
var tmp={capable:false,support:{builtin:false,plugin:false},prefixes:_25};
for(var i=0;i<_26.length;i++){
tmp[_26[i]]=false;
}
return tmp;
}
return {name:"",ver:dojo.version,os:{win:false,linux:false,osx:false},html:vscaffold(["html"],["ie","opera","khtml","safari","moz"]),svg:vscaffold(["svg"],["corel","adobe","batik"]),vml:vscaffold(["vml"],["ie"]),swf:vscaffold(["Swf","Flash","Mm"],["mm"]),swt:vscaffold(["Swt"],["ibm"])};
})();
dojo.hostenv=(function(){
var _29={isDebug:false,allowQueryConfig:false,baseScriptUri:"",baseRelativePath:"",libraryScriptUri:"",iePreventClobber:false,ieClobberMinimal:true,preventBackButtonFix:true,delayMozLoadingFix:false,searchIds:[],parseWidgets:true};
if(typeof djConfig=="undefined"){
djConfig=_29;
}else{
for(var _2a in _29){
if(typeof djConfig[_2a]=="undefined"){
djConfig[_2a]=_29[_2a];
}
}
}
return {name_:"(unset)",version_:"(unset)",getName:function(){
return this.name_;
},getVersion:function(){
return this.version_;
},getText:function(uri){
dojo.unimplemented("getText","uri="+uri);
}};
})();
dojo.hostenv.getBaseScriptUri=function(){
if(djConfig.baseScriptUri.length){
return djConfig.baseScriptUri;
}
var uri=new String(djConfig.libraryScriptUri||djConfig.baseRelativePath);
if(!uri){
dojo.raise("Nothing returned by getLibraryScriptUri(): "+uri);
}
djConfig.baseScriptUri=djConfig.baseRelativePath;
return djConfig.baseScriptUri;
};
(function(){
var _2d={pkgFileName:"__package__",loading_modules_:{},loaded_modules_:{},addedToLoadingCount:[],removedFromLoadingCount:[],inFlightCount:0,modulePrefixes_:{dojo:{name:"dojo",value:"src"}},registerModulePath:function(_2e,_2f){
this.modulePrefixes_[_2e]={name:_2e,value:_2f};
},moduleHasPrefix:function(_30){
var mp=this.modulePrefixes_;
return Boolean(mp[_30]&&mp[_30].value);
},getModulePrefix:function(_32){
if(this.moduleHasPrefix(_32)){
return this.modulePrefixes_[_32].value;
}
return _32;
},getTextStack:[],loadUriStack:[],loadedUris:[],post_load_:false,modulesLoadedListeners:[],unloadListeners:[],loadNotifying:false};
for(var _33 in _2d){
dojo.hostenv[_33]=_2d[_33];
}
})();
dojo.hostenv.loadPath=function(_34,_35,cb){
var uri;
if(_34.charAt(0)=="/"||_34.match(/^\w+:/)){
uri=_34;
}else{
uri=this.getBaseScriptUri()+_34;
}
if(djConfig.cacheBust&&dojo.render.html.capable){
uri+="?"+String(djConfig.cacheBust).replace(/\W+/g,"");
}
try{
return !_35?this.loadUri(uri,cb):this.loadUriAndCheck(uri,_35,cb);
}
catch(e){
dojo.debug(e);
return false;
}
};
dojo.hostenv.loadUri=function(uri,cb){
if(this.loadedUris[uri]){
return true;
}
var _3a=this.getText(uri,null,true);
if(!_3a){
return false;
}
this.loadedUris[uri]=true;
if(cb){
_3a="("+_3a+")";
}
var _3b=dj_eval(_3a);
if(cb){
cb(_3b);
}
return true;
};
dojo.hostenv.loadUriAndCheck=function(uri,_3d,cb){
var ok=true;
try{
ok=this.loadUri(uri,cb);
}
catch(e){
dojo.debug("failed loading ",uri," with error: ",e);
}
return Boolean(ok&&this.findModule(_3d,false));
};
dojo.loaded=function(){
};
dojo.unloaded=function(){
};
dojo.hostenv.loaded=function(){
this.loadNotifying=true;
this.post_load_=true;
var mll=this.modulesLoadedListeners;
for(var x=0;x<mll.length;x++){
mll[x]();
}
this.modulesLoadedListeners=[];
this.loadNotifying=false;
dojo.loaded();
};
dojo.hostenv.unloaded=function(){
var mll=this.unloadListeners;
while(mll.length){
(mll.pop())();
}
dojo.unloaded();
};
dojo.addOnLoad=function(obj,_44){
var dh=dojo.hostenv;
if(arguments.length==1){
dh.modulesLoadedListeners.push(obj);
}else{
if(arguments.length>1){
dh.modulesLoadedListeners.push(function(){
obj[_44]();
});
}
}
if(dh.post_load_&&dh.inFlightCount==0&&!dh.loadNotifying){
dh.callLoaded();
}
};
dojo.addOnUnload=function(obj,_47){
var dh=dojo.hostenv;
if(arguments.length==1){
dh.unloadListeners.push(obj);
}else{
if(arguments.length>1){
dh.unloadListeners.push(function(){
obj[_47]();
});
}
}
};
dojo.hostenv.modulesLoaded=function(){
if(this.post_load_){
return;
}
if(this.loadUriStack.length==0&&this.getTextStack.length==0){
if(this.inFlightCount>0){
dojo.debug("files still in flight!");
return;
}
dojo.hostenv.callLoaded();
}
};
dojo.hostenv.callLoaded=function(){
if(typeof setTimeout=="object"||(djConfig["useXDomain"]&&dojo.render.html.opera)){
setTimeout("dojo.hostenv.loaded();",0);
}else{
dojo.hostenv.loaded();
}
};
dojo.hostenv.getModuleSymbols=function(_49){
var _4a=_49.split(".");
for(var i=_4a.length;i>0;i--){
var _4c=_4a.slice(0,i).join(".");
if((i==1)&&!this.moduleHasPrefix(_4c)){
_4a[0]="../"+_4a[0];
}else{
var _4d=this.getModulePrefix(_4c);
if(_4d!=_4c){
_4a.splice(0,i,_4d);
break;
}
}
}
return _4a;
};
dojo.hostenv._global_omit_module_check=false;
dojo.hostenv.loadModule=function(_4e,_4f,_50){
if(!_4e){
return;
}
_50=this._global_omit_module_check||_50;
var _51=this.findModule(_4e,false);
if(_51){
return _51;
}
if(dj_undef(_4e,this.loading_modules_)){
this.addedToLoadingCount.push(_4e);
}
this.loading_modules_[_4e]=1;
var _52=_4e.replace(/\./g,"/")+".js";
var _53=_4e.split(".");
var _54=this.getModuleSymbols(_4e);
var _55=((_54[0].charAt(0)!="/")&&!_54[0].match(/^\w+:/));
var _56=_54[_54.length-1];
var ok;
if(_56=="*"){
_4e=_53.slice(0,-1).join(".");
while(_54.length){
_54.pop();
_54.push(this.pkgFileName);
_52=_54.join("/")+".js";
if(_55&&_52.charAt(0)=="/"){
_52=_52.slice(1);
}
ok=this.loadPath(_52,!_50?_4e:null);
if(ok){
break;
}
_54.pop();
}
}else{
_52=_54.join("/")+".js";
_4e=_53.join(".");
var _58=!_50?_4e:null;
ok=this.loadPath(_52,_58);
if(!ok&&!_4f){
_54.pop();
while(_54.length){
_52=_54.join("/")+".js";
ok=this.loadPath(_52,_58);
if(ok){
break;
}
_54.pop();
_52=_54.join("/")+"/"+this.pkgFileName+".js";
if(_55&&_52.charAt(0)=="/"){
_52=_52.slice(1);
}
ok=this.loadPath(_52,_58);
if(ok){
break;
}
}
}
if(!ok&&!_50){
dojo.raise("Could not load '"+_4e+"'; last tried '"+_52+"'");
}
}
if(!_50&&!this["isXDomain"]){
_51=this.findModule(_4e,false);
if(!_51){
dojo.raise("symbol '"+_4e+"' is not defined after loading '"+_52+"'");
}
}
return _51;
};
dojo.hostenv.startPackage=function(_59){
var _5a=String(_59);
var _5b=_5a;
var _5c=_59.split(/\./);
if(_5c[_5c.length-1]=="*"){
_5c.pop();
_5b=_5c.join(".");
}
var _5d=dojo.getObject(_5b,true);
this.loaded_modules_[_5a]=_5d;
this.loaded_modules_[_5b]=_5d;
return _5d;
};
dojo.hostenv.findModule=function(_5e,_5f){
var lmn=String(_5e);
if(this.loaded_modules_[lmn]){
return this.loaded_modules_[lmn];
}
if(_5f){
dojo.raise("no loaded module named '"+_5e+"'");
}
return null;
};
dojo.kwCompoundRequire=function(_61){
var _62=_61["common"]||[];
var _63=_62.concat(_61[dojo.hostenv.name_]||_61["default"]||[]);
for(var x=0;x<_63.length;x++){
var _65=_63[x];
if(_65.constructor==Array){
dojo.hostenv.loadModule.apply(dojo.hostenv,_65);
}else{
dojo.hostenv.loadModule(_65);
}
}
};
dojo.require=function(_66){
dojo.hostenv.loadModule.apply(dojo.hostenv,arguments);
};
dojo.requireIf=function(_67,_68){
var _69=arguments[0];
if((_69===true)||(_69=="common")||(_69&&dojo.render[_69].capable)){
var _6a=[];
for(var i=1;i<arguments.length;i++){
_6a.push(arguments[i]);
}
dojo.require.apply(dojo,_6a);
}
};
dojo.requireAfterIf=dojo.requireIf;
dojo.provide=function(_6c){
return dojo.hostenv.startPackage.apply(dojo.hostenv,arguments);
};
dojo.registerModulePath=function(_6d,_6e){
return dojo.hostenv.registerModulePath(_6d,_6e);
};
if(djConfig["modulePaths"]){
for(var param in djConfig["modulePaths"]){
dojo.registerModulePath(param,djConfig["modulePaths"][param]);
}
}
dojo.requireLocalization=function(_6f,_70,_71,_72){
dojo.require("dojo.i18n.loader");
dojo.i18n._requireLocalization.apply(dojo.hostenv,arguments);
};
}
if(typeof window!="undefined"){
(function(){
if(djConfig.allowQueryConfig){
var _73=document.location.toString();
var _74=_73.split("?",2);
if(_74.length>1){
var _75=_74[1];
var _76=_75.split("&");
for(var x in _76){
var sp=_76[x].split("=");
if((sp[0].length>9)&&(sp[0].substr(0,9)=="djConfig.")){
var opt=sp[0].substr(9);
try{
djConfig[opt]=eval(sp[1]);
}
catch(e){
djConfig[opt]=sp[1];
}
}
}
}
}
if(((djConfig["baseScriptUri"]=="")||(djConfig["baseRelativePath"]==""))&&(document&&document.getElementsByTagName)){
var _7a=document.getElementsByTagName("script");
var _7b=/(__package__|dojo|bootstrap1)\.js([\?\.]|$)/i;
for(var i=0;i<_7a.length;i++){
var src=_7a[i].getAttribute("src");
if(!src){
continue;
}
var m=src.match(_7b);
if(m){
var _7f=src.substring(0,m.index);
if(src.indexOf("bootstrap1")>-1){
_7f+="../";
}
if(!this["djConfig"]){
djConfig={};
}
if(djConfig["baseScriptUri"]==""){
djConfig["baseScriptUri"]=_7f;
}
if(djConfig["baseRelativePath"]==""){
djConfig["baseRelativePath"]=_7f;
}
break;
}
}
}
var dr=dojo.render;
var drh=dojo.render.html;
var drs=dojo.render.svg;
var dua=(drh.UA=navigator.userAgent);
var dav=(drh.AV=navigator.appVersion);
var t=true;
var f=false;
drh.capable=t;
drh.support.builtin=t;
dr.ver=parseFloat(drh.AV);
dr.os.mac=dav.indexOf("Macintosh")>=0;
dr.os.win=dav.indexOf("Windows")>=0;
dr.os.linux=dav.indexOf("X11")>=0;
drh.opera=dua.indexOf("Opera")>=0;
drh.khtml=(dav.indexOf("Konqueror")>=0)||(dav.indexOf("Safari")>=0);
drh.safari=dav.indexOf("Safari")>=0;
var _87=dua.indexOf("Gecko");
drh.mozilla=drh.moz=(_87>=0)&&(!drh.khtml);
if(drh.mozilla){
drh.geckoVersion=dua.substring(_87+6,_87+14);
}
drh.ie=(document.all)&&(!drh.opera);
drh.ie50=drh.ie&&dav.indexOf("MSIE 5.0")>=0;
drh.ie55=drh.ie&&dav.indexOf("MSIE 5.5")>=0;
drh.ie60=drh.ie&&dav.indexOf("MSIE 6.0")>=0;
drh.ie70=drh.ie&&dav.indexOf("MSIE 7.0")>=0;
var cm=document["compatMode"];
drh.quirks=(cm=="BackCompat")||(cm=="QuirksMode")||drh.ie55||drh.ie50;
dojo.locale=dojo.locale||(drh.ie?navigator.userLanguage:navigator.language).toLowerCase();
dr.vml.capable=drh.ie;
drs.capable=f;
drs.support.plugin=f;
drs.support.builtin=f;
var _89=window["document"];
var tdi=_89["implementation"];
if((tdi)&&(tdi["hasFeature"])&&(tdi.hasFeature("org.w3c.dom.svg","1.0"))){
drs.capable=t;
drs.support.builtin=t;
drs.support.plugin=f;
}
if(drh.safari){
var tmp=dua.split("AppleWebKit/")[1];
var ver=parseFloat(tmp.split(" ")[0]);
if(ver>=420){
drs.capable=t;
drs.support.builtin=t;
drs.support.plugin=f;
}
}else{
}
})();
dojo.hostenv.startPackage("dojo.hostenv");
dojo.render.name=dojo.hostenv.name_="browser";
dojo.hostenv.searchIds=[];
dojo.hostenv._XMLHTTP_PROGIDS=["Msxml2.XMLHTTP","Microsoft.XMLHTTP","Msxml2.XMLHTTP.4.0"];
dojo.hostenv.getXmlhttpObject=function(){
var _8d=null;
var _8e=null;
try{
_8d=new XMLHttpRequest();
}
catch(e){
}
if(!_8d){
for(var i=0;i<3;++i){
var _90=dojo.hostenv._XMLHTTP_PROGIDS[i];
try{
_8d=new ActiveXObject(_90);
}
catch(e){
_8e=e;
}
if(_8d){
dojo.hostenv._XMLHTTP_PROGIDS=[_90];
break;
}
}
}
if(!_8d){
return dojo.raise("XMLHTTP not available",_8e);
}
return _8d;
};
dojo.hostenv._blockAsync=false;
dojo.hostenv.getText=function(uri,_92,_93){
if(!_92){
this._blockAsync=true;
}
var _94=this.getXmlhttpObject();
function isDocumentOk(_95){
var _96=_95["status"];
return Boolean((!_96)||((200<=_96)&&(300>_96))||(_96==304));
}
if(_92){
var _97=this,_98=null,gbl=dojo.global();
var xhr=dojo.getObject("dojo.io.XMLHTTPTransport");
_94.onreadystatechange=function(){
if(_98){
gbl.clearTimeout(_98);
_98=null;
}
if(_97._blockAsync||(xhr&&xhr._blockAsync)){
_98=gbl.setTimeout(function(){
_94.onreadystatechange.apply(this);
},10);
}else{
if(4==_94.readyState){
if(isDocumentOk(_94)){
_92(_94.responseText);
}
}
}
};
}
_94.open("GET",uri,_92?true:false);
try{
_94.send(null);
if(_92){
return null;
}
if(!isDocumentOk(_94)){
var err=Error("Unable to load "+uri+" status:"+_94.status);
err.status=_94.status;
err.responseText=_94.responseText;
throw err;
}
}
catch(e){
this._blockAsync=false;
if((_93)&&(!_92)){
return null;
}else{
throw e;
}
}
this._blockAsync=false;
return _94.responseText;
};
dojo.hostenv.defaultDebugContainerId="dojoDebug";
dojo.hostenv._println_buffer=[];
dojo.hostenv._println_safe=false;
dojo.hostenv.println=function(_9c){
if(!dojo.hostenv._println_safe){
dojo.hostenv._println_buffer.push(_9c);
}else{
try{
var _9d=document.getElementById(djConfig.debugContainerId?djConfig.debugContainerId:dojo.hostenv.defaultDebugContainerId);
if(!_9d){
_9d=dojo.body();
}
var div=document.createElement("div");
div.appendChild(document.createTextNode(_9c));
_9d.appendChild(div);
}
catch(e){
try{
document.write("<div>"+_9c+"</div>");
}
catch(e2){
window.status=_9c;
}
}
}
};
dojo.addOnLoad(function(){
dojo.hostenv._println_safe=true;
while(dojo.hostenv._println_buffer.length>0){
dojo.hostenv.println(dojo.hostenv._println_buffer.shift());
}
});
function dj_addNodeEvtHdlr(_9f,_a0,fp){
var _a2=_9f["on"+_a0]||function(){
};
_9f["on"+_a0]=function(){
fp.apply(_9f,arguments);
_a2.apply(_9f,arguments);
};
return true;
}
function dj_load_init(e){
var _a4=(e&&e.type)?e.type.toLowerCase():"load";
if(arguments.callee.initialized||(_a4!="domcontentloaded"&&_a4!="load")){
return;
}
arguments.callee.initialized=true;
if(typeof (_timer)!="undefined"){
clearInterval(_timer);
delete _timer;
}
var _a5=function(){
if(dojo.render.html.ie){
dojo.hostenv.makeWidgets();
}
};
if(dojo.hostenv.inFlightCount==0){
_a5();
dojo.hostenv.modulesLoaded();
}else{
dojo.hostenv.modulesLoadedListeners.unshift(_a5);
}
}
if(document.addEventListener){
if(dojo.render.html.opera||(dojo.render.html.moz&&(djConfig["enableMozDomContentLoaded"]===true))){
document.addEventListener("DOMContentLoaded",dj_load_init,null);
}
window.addEventListener("load",dj_load_init,null);
}
if(dojo.render.html.ie&&dojo.render.os.win){
document.write("<scr"+"ipt defer src=\"//:\" "+"onreadystatechange=\"if(this.readyState=='complete'){dj_load_init();}\">"+"</scr"+"ipt>");
}
if(/(WebKit|khtml)/i.test(navigator.userAgent)){
var _timer=setInterval(function(){
if(/loaded|complete/.test(document.readyState)){
dj_load_init();
}
},10);
}
if(dojo.render.html.ie){
dj_addNodeEvtHdlr(window,"beforeunload",function(){
dojo.hostenv._unloading=true;
window.setTimeout(function(){
dojo.hostenv._unloading=false;
},0);
});
}
dj_addNodeEvtHdlr(window,"unload",function(){
if((!dojo.render.html.ie)||(dojo.render.html.ie&&dojo.hostenv._unloading)){
dojo.hostenv.unloaded();
}
});
dojo.hostenv.makeWidgets=function(){
var _a6=[];
if(djConfig.searchIds&&djConfig.searchIds.length>0){
_a6=_a6.concat(djConfig.searchIds);
}
if(dojo.hostenv.searchIds&&dojo.hostenv.searchIds.length>0){
_a6=_a6.concat(dojo.hostenv.searchIds);
}
if((djConfig.parseWidgets)||(_a6.length>0)){
if(dojo.getObject("dojo.widget.Parse")){
var _a7=new dojo.xml.Parse();
if(_a6.length>0){
for(var x=0;x<_a6.length;x++){
var _a9=document.getElementById(_a6[x]);
if(!_a9){
continue;
}
var _aa=_a7.parseElement(_a9,null,true);
dojo.widget.getParser().createComponents(_aa);
}
}else{
if(djConfig.parseWidgets){
var _aa=_a7.parseElement(dojo.body(),null,true);
dojo.widget.getParser().createComponents(_aa);
}
}
}
}
};
dojo.addOnLoad(function(){
if(!dojo.render.html.ie){
dojo.hostenv.makeWidgets();
}
});
try{
if(dojo.render.html.ie){
document.namespaces.add("v","urn:schemas-microsoft-com:vml");
document.createStyleSheet().addRule("v\\:*","behavior:url(#default#VML)");
}
}
catch(e){
}
dojo.hostenv.writeIncludes=function(){
};
if(!dj_undef("document",this)){
dj_currentDocument=this.document;
}
dojo.doc=function(){
return dj_currentDocument;
};
dojo.body=function(){
return dojo.doc().body||dojo.doc().getElementsByTagName("body")[0];
};
dojo.byId=function(id,doc){
if((id)&&((typeof id=="string")||(id instanceof String))){
if(!doc){
doc=dj_currentDocument;
}
var ele=doc.getElementById(id);
if(ele&&(ele.id!=id)&&doc.all){
ele=null;
eles=doc.all[id];
if(eles){
if(eles.length){
for(var i=0;i<eles.length;i++){
if(eles[i].id==id){
ele=eles[i];
break;
}
}
}else{
ele=eles;
}
}
}
return ele;
}
return id;
};
dojo.setContext=function(_af,_b0){
dj_currentContext=_af;
dj_currentDocument=_b0;
};
dojo._fireCallback=function(_b1,_b2,_b3){
if((_b2)&&((typeof _b1=="string")||(_b1 instanceof String))){
_b1=_b2[_b1];
}
return (_b2?_b1.apply(_b2,_b3||[]):_b1());
};
dojo.withGlobal=function(_b4,_b5,_b6,_b7){
var _b8;
var _b9=dj_currentContext;
var _ba=dj_currentDocument;
try{
dojo.setContext(_b4,_b4.document);
_b8=dojo._fireCallback(_b5,_b6,_b7);
}
finally{
dojo.setContext(_b9,_ba);
}
return _b8;
};
dojo.withDoc=function(_bb,_bc,_bd,_be){
var _bf;
var _c0=dj_currentDocument;
try{
dj_currentDocument=_bb;
_bf=dojo._fireCallback(_bc,_bd,_be);
}
finally{
dj_currentDocument=_c0;
}
return _bf;
};
}
dojo.requireIf((djConfig["isDebug"]||djConfig["debugAtAllCosts"]),"dojo.debug");
dojo.requireIf(djConfig["debugAtAllCosts"]&&!window.widget&&!djConfig["useXDomain"],"dojo.browser_debug");
dojo.requireIf(djConfig["debugAtAllCosts"]&&!window.widget&&djConfig["useXDomain"],"dojo.browser_debug_xd");
dojo.provide("dojo.lang.common");
dojo.lang.inherits=function(_c1,_c2){
if(!dojo.lang.isFunction(_c2)){
dojo.raise("dojo.inherits: superclass argument ["+_c2+"] must be a function (subclass: ["+_c1+"']");
}
_c1.prototype=new _c2();
_c1.prototype.constructor=_c1;
_c1.superclass=_c2.prototype;
_c1["super"]=_c2.prototype;
};
dojo.lang._mixin=function(obj,_c4){
var _c5={};
for(var x in _c4){
if((typeof _c5[x]=="undefined")||(_c5[x]!=_c4[x])){
obj[x]=_c4[x];
}
}
if(dojo.render.html.ie&&(typeof (_c4["toString"])=="function")&&(_c4["toString"]!=obj["toString"])&&(_c4["toString"]!=_c5["toString"])){
obj.toString=_c4.toString;
}
return obj;
};
dojo.lang.mixin=function(obj,_c8){
for(var i=1,l=arguments.length;i<l;i++){
dojo.lang._mixin(obj,arguments[i]);
}
return obj;
};
dojo.lang.extend=function(_cb,_cc){
for(var i=1,l=arguments.length;i<l;i++){
dojo.lang._mixin(_cb.prototype,arguments[i]);
}
return _cb;
};
dojo.lang._delegate=function(obj,_d0){
function TMP(){
}
TMP.prototype=obj;
var tmp=new TMP();
if(_d0){
dojo.lang.mixin(tmp,_d0);
}
return tmp;
};
dojo.inherits=dojo.lang.inherits;
dojo.mixin=dojo.lang.mixin;
dojo.extend=dojo.lang.extend;
dojo.lang.find=function(_d2,_d3,_d4,_d5){
var _d6=dojo.lang.isString(_d2);
if(_d6){
_d2=_d2.split("");
}
if(_d5){
var _d7=-1;
var i=_d2.length-1;
var end=-1;
}else{
var _d7=1;
var i=0;
var end=_d2.length;
}
if(_d4){
while(i!=end){
if(_d2[i]===_d3){
return i;
}
i+=_d7;
}
}else{
while(i!=end){
if(_d2[i]==_d3){
return i;
}
i+=_d7;
}
}
return -1;
};
dojo.lang.indexOf=dojo.lang.find;
dojo.lang.findLast=function(_da,_db,_dc){
return dojo.lang.find(_da,_db,_dc,true);
};
dojo.lang.lastIndexOf=dojo.lang.findLast;
dojo.lang.inArray=function(_dd,_de){
return dojo.lang.find(_dd,_de)>-1;
};
dojo.lang.isObject=function(it){
if(typeof it=="undefined"){
return false;
}
return (typeof it=="object"||it===null||dojo.lang.isArray(it)||dojo.lang.isFunction(it));
};
dojo.lang.isArray=function(it){
return (it&&it instanceof Array||typeof it=="array");
};
dojo.lang.isArrayLike=function(it){
if((!it)||(dojo.lang.isUndefined(it))){
return false;
}
if(dojo.lang.isString(it)){
return false;
}
if(dojo.lang.isFunction(it)){
return false;
}
if(dojo.lang.isArray(it)){
return true;
}
if((it.tagName)&&(it.tagName.toLowerCase()=="form")){
return false;
}
if(dojo.lang.isNumber(it.length)&&isFinite(it.length)){
return true;
}
return false;
};
dojo.lang.isFunction=function(it){
return (it instanceof Function||typeof it=="function");
};
(function(){
if((dojo.render.html.capable)&&(dojo.render.html["safari"])){
dojo.lang.isFunction=function(it){
if((typeof (it)=="function")&&(it=="[object NodeList]")){
return false;
}
return (it instanceof Function||typeof it=="function");
};
}
})();
dojo.lang.isString=function(it){
return (typeof it=="string"||it instanceof String);
};
dojo.lang.isAlien=function(it){
if(!it){
return false;
}
return !dojo.lang.isFunction(it)&&/\{\s*\[native code\]\s*\}/.test(String(it));
};
dojo.lang.isBoolean=function(it){
return (it instanceof Boolean||typeof it=="boolean");
};
dojo.lang.isNumber=function(it){
return (it instanceof Number||typeof it=="number");
};
dojo.lang.isUndefined=function(it){
return ((typeof (it)=="undefined")&&(it==undefined));
};
dojo.provide("dojo.lang.func");
dojo.lang.hitch=function(_e9,_ea){
var _eb=[];
for(var x=2;x<arguments.length;x++){
_eb.push(arguments[x]);
}
var fcn=(dojo.lang.isString(_ea)?_e9[_ea]:_ea)||function(){
};
return function(){
var ta=_eb.concat([]);
for(var x=0;x<arguments.length;x++){
ta.push(arguments[x]);
}
return fcn.apply(_e9,ta);
};
};
dojo.lang.anonCtr=0;
dojo.lang.anon={};
dojo.lang.nameAnonFunc=function(_f0,_f1,_f2){
var _f3=(dojo.render.html.capable&&dojo.render.html["ie"]);
var jpn="$joinpoint";
var nso=(_f1||dojo.lang.anon);
if(_f3){
var cn=_f0["__dojoNameCache"];
if(cn&&nso[cn]===_f0){
return _f0["__dojoNameCache"];
}else{
if(cn){
var _f7=cn.indexOf(jpn);
if(_f7!=-1){
return cn.substring(0,_f7);
}
}
}
}
if((_f2)||((dj_global["djConfig"])&&(djConfig["slowAnonFuncLookups"]==true))){
for(var x in nso){
try{
if(nso[x]===_f0){
if(_f3){
_f0["__dojoNameCache"]=x;
var _f7=x.indexOf(jpn);
if(_f7!=-1){
x=x.substring(0,_f7);
}
}
return x;
}
}
catch(e){
}
}
}
var ret="__"+dojo.lang.anonCtr++;
while(typeof nso[ret]!="undefined"){
ret="__"+dojo.lang.anonCtr++;
}
nso[ret]=_f0;
return ret;
};
dojo.lang.forward=function(_fa){
return function(){
return this[_fa].apply(this,arguments);
};
};
dojo.lang.curry=function(_fb,_fc){
var _fd=[];
_fb=_fb||dj_global;
if(dojo.lang.isString(_fc)){
_fc=_fb[_fc];
}
for(var x=2;x<arguments.length;x++){
_fd.push(arguments[x]);
}
var _ff=(_fc["__preJoinArity"]||_fc.length)-_fd.length;
function gather(_100,_101,_102){
var _103=_102;
var _104=_101.slice(0);
for(var x=0;x<_100.length;x++){
_104.push(_100[x]);
}
_102=_102-_100.length;
if(_102<=0){
var res=_fc.apply(_fb,_104);
_102=_103;
return res;
}else{
return function(){
return gather(arguments,_104,_102);
};
}
}
return gather([],_fd,_ff);
};
dojo.lang.curryArguments=function(_107,func,args,_10a){
var _10b=[];
var x=_10a||0;
for(x=_10a;x<args.length;x++){
_10b.push(args[x]);
}
return dojo.lang.curry.apply(dojo.lang,[_107,func].concat(_10b));
};
dojo.lang.tryThese=function(){
for(var x=0;x<arguments.length;x++){
try{
if(typeof arguments[x]=="function"){
var ret=(arguments[x]());
if(ret){
return ret;
}
}
}
catch(e){
dojo.debug(e);
}
}
};
dojo.lang.delayThese=function(farr,cb,_111,_112){
if(!farr.length){
if(typeof _112=="function"){
_112();
}
return;
}
if((typeof _111=="undefined")&&(typeof cb=="number")){
_111=cb;
cb=function(){
};
}else{
if(!cb){
cb=function(){
};
if(!_111){
_111=0;
}
}
}
setTimeout(function(){
(farr.shift())();
cb();
dojo.lang.delayThese(farr,cb,_111,_112);
},_111);
};
dojo.provide("dojo.string.common");
dojo.string.trim=function(str,wh){
if(!str.replace){
return str;
}
if(!str.length){
return str;
}
var re=(wh>0)?(/^\s+/):(wh<0)?(/\s+$/):(/^\s+|\s+$/g);
return str.replace(re,"");
};
dojo.string.trimStart=function(str){
return dojo.string.trim(str,1);
};
dojo.string.trimEnd=function(str){
return dojo.string.trim(str,-1);
};
dojo.string.repeat=function(str,_119,_11a){
var out="";
for(var i=0;i<_119;i++){
out+=str;
if(_11a&&i<_119-1){
out+=_11a;
}
}
return out;
};
dojo.string.pad=function(str,len,c,dir){
var out=String(str);
if(!c){
c="0";
}
if(!dir){
dir=1;
}
while(out.length<len){
if(dir>0){
out=c+out;
}else{
out+=c;
}
}
return out;
};
dojo.string.padLeft=function(str,len,c){
return dojo.string.pad(str,len,c,1);
};
dojo.string.padRight=function(str,len,c){
return dojo.string.pad(str,len,c,-1);
};
dojo.provide("dojo.lang.array");
dojo.lang.mixin(dojo.lang,{has:function(obj,name){
try{
return typeof obj[name]!="undefined";
}
catch(e){
return false;
}
},isEmpty:function(obj){
if(dojo.lang.isArrayLike(obj)||dojo.lang.isString(obj)){
return obj.length===0;
}else{
if(dojo.lang.isObject(obj)){
var tmp={};
for(var x in obj){
if(obj[x]&&(!tmp[x])){
return false;
}
}
return true;
}
}
},map:function(arr,obj,_12f){
var _130=dojo.lang.isString(arr);
if(_130){
arr=arr.split("");
}
if(dojo.lang.isFunction(obj)&&(!_12f)){
_12f=obj;
obj=dj_global;
}else{
if(dojo.lang.isFunction(obj)&&_12f){
var _131=obj;
obj=_12f;
_12f=_131;
}
}
if(Array.map){
var _132=Array.map(arr,_12f,obj);
}else{
var _132=[];
for(var i=0;i<arr.length;++i){
_132.push(_12f.call(obj,arr[i]));
}
}
if(_130){
return _132.join("");
}else{
return _132;
}
},reduce:function(arr,_135,_136,_137){
var _138=_136;
if(arguments.length==2){
_138=arr[0];
arr=arr.slice(1);
}
var ob=_137||dj_global;
dojo.lang.map(arr,function(val){
_138=_135.call(ob,_138,val);
});
return _138;
},forEach:function(_13b,_13c,_13d){
if(dojo.lang.isString(_13b)){
_13b=_13b.split("");
}
if(Array.forEach){
Array.forEach(_13b,_13c,_13d);
}else{
if(!_13d){
_13d=dj_global;
}
for(var i=0,l=_13b.length;i<l;i++){
_13c.call(_13d,_13b[i],i,_13b);
}
}
},_everyOrSome:function(_140,arr,_142,_143){
if(dojo.lang.isString(arr)){
arr=arr.split("");
}
if(Array.every){
return Array[_140?"every":"some"](arr,_142,_143);
}else{
if(!_143){
_143=dj_global;
}
for(var i=0,l=arr.length;i<l;i++){
var _146=_142.call(_143,arr[i],i,arr);
if(_140&&!_146){
return false;
}else{
if((!_140)&&(_146)){
return true;
}
}
}
return Boolean(_140);
}
},every:function(arr,_148,_149){
return this._everyOrSome(true,arr,_148,_149);
},some:function(arr,_14b,_14c){
return this._everyOrSome(false,arr,_14b,_14c);
},filter:function(arr,_14e,_14f){
var _150=dojo.lang.isString(arr);
if(_150){
arr=arr.split("");
}
var _151;
if(Array.filter){
_151=Array.filter(arr,_14e,_14f);
}else{
if(!_14f){
if(arguments.length>=3){
dojo.raise("thisObject doesn't exist!");
}
_14f=dj_global;
}
_151=[];
for(var i=0;i<arr.length;i++){
if(_14e.call(_14f,arr[i],i,arr)){
_151.push(arr[i]);
}
}
}
if(_150){
return _151.join("");
}else{
return _151;
}
},unnest:function(){
var out=[];
for(var i=0;i<arguments.length;i++){
if(dojo.lang.isArrayLike(arguments[i])){
var add=dojo.lang.unnest.apply(this,arguments[i]);
out=out.concat(add);
}else{
out.push(arguments[i]);
}
}
return out;
},toArray:function(_156,_157){
var _158=[];
for(var i=_157||0;i<_156.length;i++){
_158.push(_156[i]);
}
return _158;
}});
dojo.provide("dojo.string.extras");
dojo.string.substitute=function(_15a,map,_15c,_15d){
return _15a.replace(/\$\{([^\s\:]+)(?:\:(\S+))?\}/g,function(_15e,key,_160){
var _161=dojo.getObject(key,false,map).toString();
if(_160){
_161=dojo.getObject(_160,false,_15d)(_161);
}
if(_15c){
_161=_15c(_161);
}
return _161;
});
};
dojo.string.capitalize=function(str){
if(!dojo.lang.isString(str)){
return "";
}
return str.replace(/[^\s]+/g,function(word){
return word.substring(0,1).toUpperCase()+word.substring(1);
});
};
dojo.string.isBlank=function(str){
if(!dojo.lang.isString(str)){
return true;
}
return (dojo.string.trim(str).length==0);
};
dojo.string.encodeAscii=function(str){
if(!dojo.lang.isString(str)){
return str;
}
var ret="";
var _167=escape(str);
var _168,re=/%u([0-9A-F]{4})/i;
while((_168=_167.match(re))){
var num=Number("0x"+_168[1]);
var _16b=escape("&#"+num+";");
ret+=_167.substring(0,_168.index)+_16b;
_167=_167.substring(_168.index+_168[0].length);
}
ret+=_167.replace(/\+/g,"%2B");
return ret;
};
dojo.string.escape=function(type,str){
var args=dojo.lang.toArray(arguments,1);
switch(type.toLowerCase()){
case "xml":
case "html":
case "xhtml":
return dojo.string.escapeXml.apply(this,args);
case "sql":
return dojo.string.escapeSql.apply(this,args);
case "regexp":
case "regex":
return dojo.string.escapeRegExp.apply(this,args);
case "javascript":
case "jscript":
case "js":
return dojo.string.escapeJavaScript.apply(this,args);
case "ascii":
return dojo.string.encodeAscii.apply(this,args);
default:
return str;
}
};
dojo.string.escapeXml=function(str,_170){
str=str.replace(/&/gm,"&amp;").replace(/</gm,"&lt;").replace(/>/gm,"&gt;").replace(/"/gm,"&quot;");
if(!_170){
str=str.replace(/'/gm,"&#39;");
}
return str;
};
dojo.string.escapeSql=function(str){
return str.replace(/'/gm,"''");
};
dojo.string.escapeRegExp=function(str,_173){
return str.replace(/([\.$?*!=:|{}\(\)\[\]\\\/^])/g,function(ch){
if(_173&&_173.indexOf(ch)!=-1){
return ch;
}
return "\\"+ch;
});
};
dojo.string.escapeJavaScript=function(str){
return str.replace(/(["'\f\b\n\t\r])/gm,"\\$1");
};
dojo.string.escapeString=function(str){
return ("\""+str.replace(/(["\\])/g,"\\$1")+"\"").replace(/[\f]/g,"\\f").replace(/[\b]/g,"\\b").replace(/[\n]/g,"\\n").replace(/[\t]/g,"\\t").replace(/[\r]/g,"\\r");
};
dojo.string.summary=function(str,len){
if(!len||str.length<=len){
return str;
}
return str.substring(0,len).replace(/\.+$/,"")+"...";
};
dojo.string.endsWith=function(str,end,_17b){
if(_17b){
str=str.toLowerCase();
end=end.toLowerCase();
}
if((str.length-end.length)<0){
return false;
}
return str.lastIndexOf(end)==str.length-end.length;
};
dojo.string.endsWithAny=function(str){
for(var i=1;i<arguments.length;i++){
if(dojo.string.endsWith(str,arguments[i])){
return true;
}
}
return false;
};
dojo.string.startsWith=function(str,_17f,_180){
if(_180){
str=str.toLowerCase();
_17f=_17f.toLowerCase();
}
return str.indexOf(_17f)==0;
};
dojo.string.startsWithAny=function(str){
for(var i=1;i<arguments.length;i++){
if(dojo.string.startsWith(str,arguments[i])){
return true;
}
}
return false;
};
dojo.string.has=function(str){
for(var i=1;i<arguments.length;i++){
if(str.indexOf(arguments[i])>-1){
return true;
}
}
return false;
};
dojo.string.normalizeNewlines=function(text,_186){
if(_186=="\n"){
text=text.replace(/\r\n/g,"\n");
text=text.replace(/\r/g,"\n");
}else{
if(_186=="\r"){
text=text.replace(/\r\n/g,"\r");
text=text.replace(/\n/g,"\r");
}else{
text=text.replace(/([^\r])\n/g,"$1\r\n").replace(/\r([^\n])/g,"\r\n$1");
}
}
return text;
};
dojo.string.splitEscaped=function(str,_188){
var _189=[];
for(var i=0,_18b=0;i<str.length;i++){
if(str.charAt(i)=="\\"){
i++;
continue;
}
if(str.charAt(i)==_188){
_189.push(str.substring(_18b,i));
_18b=i+1;
}
}
_189.push(str.substr(_18b));
return _189;
};
dojo.provide("dojo.AdapterRegistry");
dojo.AdapterRegistry=function(_18c){
this.pairs=[];
this.returnWrappers=_18c||false;
};
dojo.lang.extend(dojo.AdapterRegistry,{register:function(name,_18e,wrap,_190,_191){
var type=(_191)?"unshift":"push";
this.pairs[type]([name,_18e,wrap,_190]);
},match:function(){
for(var i=0;i<this.pairs.length;i++){
var pair=this.pairs[i];
if(pair[1].apply(this,arguments)){
if((pair[3])||(this.returnWrappers)){
return pair[2];
}else{
return pair[2].apply(this,arguments);
}
}
}
throw new Error("No match found");
},unregister:function(name){
for(var i=0;i<this.pairs.length;i++){
var pair=this.pairs[i];
if(pair[0]==name){
this.pairs.splice(i,1);
return true;
}
}
return false;
}});
dojo.provide("dojo.json");
dojo.json={jsonRegistry:new dojo.AdapterRegistry(),register:function(name,_199,wrap,_19b){
dojo.json.jsonRegistry.register(name,_199,wrap,_19b);
},evalJson:function(json){
try{
return eval("("+json+")");
}
catch(e){
dojo.debug(e);
return json;
}
},serialize:function(o){
var _19e=typeof (o);
if(_19e=="undefined"){
return "undefined";
}else{
if((_19e=="number")||(_19e=="boolean")){
return o+"";
}else{
if(o===null){
return "null";
}
}
}
if(_19e=="string"){
return dojo.string.escapeString(o);
}
var me=arguments.callee;
var _1a0;
if(typeof (o.__json__)=="function"){
_1a0=o.__json__();
if(o!==_1a0){
return me(_1a0);
}
}
if(typeof (o.json)=="function"){
_1a0=o.json();
if(o!==_1a0){
return me(_1a0);
}
}
if(_19e!="function"&&typeof (o.length)=="number"){
var res=[];
for(var i=0;i<o.length;i++){
var val=me(o[i]);
if(typeof (val)!="string"){
val="undefined";
}
res.push(val);
}
return "["+res.join(",")+"]";
}
try{
window.o=o;
_1a0=dojo.json.jsonRegistry.match(o);
return me(_1a0);
}
catch(e){
}
if(_19e=="function"){
return null;
}
res=[];
for(var k in o){
var _1a5;
if(typeof (k)=="number"){
_1a5="\""+k+"\"";
}else{
if(typeof (k)=="string"){
_1a5=dojo.string.escapeString(k);
}else{
continue;
}
}
val=me(o[k]);
if(typeof (val)!="string"){
continue;
}
res.push(_1a5+":"+val);
}
return "{"+res.join(",")+"}";
}};
dojo.provide("dojo.collections.Store");
dojo.collections.Store=function(_1a6){
var data=[];
var _1a8={};
this.keyField="Id";
this.get=function(){
return data;
};
this.getByKey=function(key){
return _1a8[key];
};
this.getByIndex=function(idx){
return data[idx];
};
this.getIndexOf=function(key){
for(var i=0;i<data.length;i++){
if(data[i].key==key){
return i;
}
}
return -1;
};
this.getData=function(){
var arr=[];
for(var i=0;i<data.length;i++){
arr.push(data[i].src);
}
return arr;
};
this.getDataByKey=function(key){
if(_1a8[key]!=null){
return _1a8[key].src;
}
return null;
};
this.getIndexOfData=function(obj){
for(var i=0;i<data.length;i++){
if(data[i].src==obj){
return i;
}
}
return -1;
};
this.getDataByIndex=function(idx){
if(data[idx]){
return data[idx].src;
}
return null;
};
this.forEach=function(fn){
if(Array.forEach){
Array.forEach(data,fn,this);
}else{
for(var i=0;i<data.length;i++){
fn.call(this,data[i]);
}
}
};
this.forEachData=function(fn){
if(Array.forEach){
Array.forEach(this.getData(),fn,this);
}else{
var a=this.getData();
for(var i=0;i<a.length;i++){
fn.call(this,a[i]);
}
}
};
this.setData=function(arr,_1b9){
this.clearData(true);
for(var i=0;i<arr.length;i++){
var o={key:arr[i][this.keyField],src:arr[i]};
data.push(o);
_1a8[o.key]=o;
}
if(!_1b9){
this.onSetData();
}
};
this.clearData=function(_1bc){
data=[];
_1a8={};
if(!_1bc){
this.onClearData();
}
};
this.update=function(obj,_1be,val,_1c0){
var _1c1=_1be.split("."),i=0,o=obj,_1c4;
if(_1c1.length>1){
_1c4=_1c1.pop();
do{
if(_1c1[i].indexOf("()")>-1){
var temp=_1c1[i++].split("()")[0];
if(!o[temp]){
dojo.raise("dojo.collections.Store.getField(obj, '"+_1c4+"'): '"+temp+"' is not a property of the passed object.");
}else{
o=o[temp]();
}
}else{
o=o[_1c1[i++]];
}
}while(i<_1c1.length&&o!=null);
}else{
_1c4=_1c1[0];
}
obj[_1c4]=val;
if(!_1c0){
this.onUpdateField(obj,_1be,val);
}
};
this.addData=this.updateData=function(obj,key,_1c8){
var k=key||obj[this.keyField];
if(_1a8[k]!=null){
var o=_1a8[k];
o.src=obj;
if(!_1c8){
this.onUpdateData(o);
}
}else{
var o={key:k,src:obj};
data.push(o);
_1a8[o.key]=o;
if(!_1c8){
this.onAddData(o);
}
}
};
this.addDataRange=this.updateDataRange=function(arr,_1cc){
var _1cd=[];
var _1ce=[];
for(var i=0;i<arr.length;i++){
var k=arr[i][this.keyField];
if(_1a8[k]!=null){
var o=_1a8[k];
o.src=arr[i];
_1ce.push(o);
}else{
var o={key:k,src:arr[i]};
data.push(o);
_1a8[k]=o;
_1cd.push(o);
}
}
if(!_1cc){
if(_1ce.length>0){
this.onUpdateDataRange(_1ce);
}
if(_1cd.length>0){
this.onAddDataRange(_1cd);
}
}
};
this.addDataByIndex=this.updateDataByIndex=function(obj,idx,key,_1d5){
var k=key||obj[this.keyField];
if(_1a8[k]!=null){
var i=this.getIndexOf(k);
var o=data.splice(i,1);
o.src=obj;
if(!_1d5){
this.onUpdateData(o);
}
}else{
var o={key:k,src:obj};
_1a8[k]=o;
if(!_1d5){
this.onAddData(o);
}
}
data.splice(idx,0,o);
};
this.addDataRangeByIndex=this.updateDataRangeByIndex=function(arr,idx,_1db){
var _1dc=[];
var _1dd=[];
var _1de=[];
for(var i=0;i<arr.length;i++){
var k=arr[i][this.keyField];
if(_1a8[k]!=null){
var j=this.getIndexOf(k);
var o=data.splice(j,1);
o.src=arr[i];
_1de.push(o);
}else{
var o={key:k,src:arr[i]};
_1a8[k]=o;
_1dd.push(o);
}
_1dc.push(o);
}
data.splice(idx,0,_1dc);
if(!_1db){
if(_1de.length>0){
this.onUpdateDataRange(_1de);
}
if(_1dd.length>0){
this.onAddDataRange(_1dd);
}
}
};
this.removeData=function(obj,_1e4){
var idx=-1;
var o=null;
for(var i=0;i<data.length;i++){
if(data[i].src==obj){
idx=i;
o=data[i];
break;
}
}
if(!_1e4){
this.onRemoveData(o);
}
if(idx>-1){
data.splice(idx,1);
delete _1a8[o.key];
}
};
this.removeDataRange=function(idx,_1e9,_1ea){
var ret=data.splice(idx,_1e9);
for(var i=0;i<ret.length;i++){
delete _1a8[ret[i].key];
}
if(!_1ea){
this.onRemoveDataRange(ret);
}
return ret;
};
this.removeDataByKey=function(key,_1ee){
this.removeData(this.getDataByKey(key),_1ee);
};
this.removeDataByIndex=function(idx,_1f0){
this.removeData(this.getDataByIndex(idx),_1f0);
};
if(_1a6&&_1a6.length&&_1a6[0]){
this.setData(_1a6,true);
}
};
dojo.extend(dojo.collections.Store,{getField:function(obj,_1f2){
var _1f3=_1f2.split("."),i=0,o=obj;
do{
if(_1f3[i].indexOf("()")>-1){
var temp=_1f3[i++].split("()")[0];
if(!o[temp]){
dojo.raise("dojo.collections.Store.getField(obj, '"+_1f2+"'): '"+temp+"' is not a property of the passed object.");
}else{
o=o[temp]();
}
}else{
o=o[_1f3[i++]];
}
}while(i<_1f3.length&&o!=null);
if(i<_1f3.length){
dojo.raise("dojo.collections.Store.getField(obj, '"+_1f2+"'): '"+_1f2+"' is not a property of the passed object.");
}
return o;
},getFromHtml:function(meta,body,_1f9){
var rows=body.rows;
var ctor=function(row){
var obj={};
for(var i=0;i<meta.length;i++){
var o=obj;
var data=row.cells[i].innerHTML;
var p=meta[i].getField();
if(p.indexOf(".")>-1){
p=p.split(".");
while(p.length>1){
var pr=p.shift();
o[pr]={};
o=o[pr];
}
p=p[0];
}
var type=meta[i].getType();
if(type==String){
o[p]=data;
}else{
if(data){
o[p]=new type(data);
}else{
o[p]=new type();
}
}
}
return obj;
};
var arr=[];
for(var i=0;i<rows.length;i++){
var o=ctor(rows[i]);
if(_1f9){
_1f9(o,rows[i]);
}
arr.push(o);
}
return arr;
},onSetData:function(){
},onClearData:function(){
},onAddData:function(obj){
},onAddDataRange:function(arr){
},onUpdateData:function(obj){
},onUpdateDataRange:function(arr){
},onRemoveData:function(obj){
},onRemoveDataRange:function(arr){
},onUpdateField:function(obj,_20e,val){
}});
dojo.provide("dojo.gfx.color");
dojo.gfx.color.Color=function(r,g,b,a){
if(dojo.lang.isArray(r)){
this.r=r[0];
this.g=r[1];
this.b=r[2];
this.a=r[3]||1;
}else{
if(dojo.lang.isString(r)){
var rgb=dojo.gfx.color.extractRGB(r);
this.r=rgb[0];
this.g=rgb[1];
this.b=rgb[2];
this.a=g||1;
}else{
if(r instanceof dojo.gfx.color.Color){
this.r=r.r;
this.b=r.b;
this.g=r.g;
this.a=r.a;
}else{
this.r=r;
this.g=g;
this.b=b;
this.a=a;
}
}
}
};
dojo.gfx.color.Color.fromArray=function(arr){
return new dojo.gfx.color.Color(arr[0],arr[1],arr[2],arr[3]);
};
dojo.extend(dojo.gfx.color.Color,{toRgb:function(_216){
if(_216){
return this.toRgba();
}else{
return [this.r,this.g,this.b];
}
},toRgba:function(){
return [this.r,this.g,this.b,this.a];
},toHex:function(){
return dojo.gfx.color.rgb2hex(this.toRgb());
},toCss:function(){
return "rgb("+this.toRgb().join()+")";
},toString:function(){
return this.toHex();
},blend:function(_217,_218){
var rgb=null;
if(dojo.lang.isArray(_217)){
rgb=_217;
}else{
if(_217 instanceof dojo.gfx.color.Color){
rgb=_217.toRgb();
}else{
rgb=new dojo.gfx.color.Color(_217).toRgb();
}
}
return dojo.gfx.color.blend(this.toRgb(),rgb,_218);
}});
dojo.gfx.color.named={white:[255,255,255],black:[0,0,0],red:[255,0,0],green:[0,255,0],lime:[0,255,0],blue:[0,0,255],navy:[0,0,128],gray:[128,128,128],silver:[192,192,192]};
dojo.gfx.color.blend=function(a,b,_21c){
if(typeof a=="string"){
return dojo.gfx.color.blendHex(a,b,_21c);
}
if(!_21c){
_21c=0;
}
_21c=Math.min(Math.max(-1,_21c),1);
_21c=((_21c+1)/2);
var c=[];
for(var x=0;x<3;x++){
c[x]=parseInt(b[x]+((a[x]-b[x])*_21c));
}
return c;
};
dojo.gfx.color.blendHex=function(a,b,_221){
return dojo.gfx.color.rgb2hex(dojo.gfx.color.blend(dojo.gfx.color.hex2rgb(a),dojo.gfx.color.hex2rgb(b),_221));
};
dojo.gfx.color.extractRGB=function(_222){
_222=_222.toLowerCase();
if(_222.indexOf("rgb")==0){
var _223=_222.match(/rgba*\((\d+), *(\d+), *(\d+)/i);
var ret=_223.splice(1,3);
return ret;
}else{
var _225=dojo.gfx.color.hex2rgb(_222);
if(_225){
return _225;
}else{
return dojo.gfx.color.named[_222]||[255,255,255];
}
}
};
dojo.gfx.color.hex2rgb=function(hex){
var _227="0123456789ABCDEF";
var rgb=new Array(3);
if(hex.indexOf("#")==0){
hex=hex.substring(1);
}
hex=hex.toUpperCase();
if(hex.replace(new RegExp("["+_227+"]","g"),"")!=""){
return null;
}
if(hex.length==3){
rgb[0]=hex.charAt(0)+hex.charAt(0);
rgb[1]=hex.charAt(1)+hex.charAt(1);
rgb[2]=hex.charAt(2)+hex.charAt(2);
}else{
rgb[0]=hex.substring(0,2);
rgb[1]=hex.substring(2,4);
rgb[2]=hex.substring(4);
}
for(var i=0;i<rgb.length;i++){
rgb[i]=_227.indexOf(rgb[i].charAt(0))*16+_227.indexOf(rgb[i].charAt(1));
}
return rgb;
};
dojo.gfx.color.rgb2hex=function(r,g,b){
if(dojo.lang.isArray(r)){
g=r[1]||0;
b=r[2]||0;
r=r[0]||0;
}
var ret=dojo.lang.map([r,g,b],function(x){
x=new Number(x);
var s=x.toString(16);
while(s.length<2){
s="0"+s;
}
return s;
});
ret.unshift("#");
return ret.join("");
};
dojo.provide("dojo.gfx.color.hsl");
dojo.lang.extend(dojo.gfx.color.Color,{toHsl:function(){
return dojo.gfx.color.rgb2hsl(this.toRgb());
}});
dojo.gfx.color.rgb2hsl=function(r,g,b){
if(dojo.lang.isArray(r)){
b=r[2]||0;
g=r[1]||0;
r=r[0]||0;
}
r/=255;
g/=255;
b/=255;
var h=null;
var s=null;
var l=null;
var min=Math.min(r,g,b);
var max=Math.max(r,g,b);
var _238=max-min;
l=(min+max)/2;
s=0;
if((l>0)&&(l<1)){
s=_238/((l<0.5)?(2*l):(2-2*l));
}
h=0;
if(_238>0){
if((max==r)&&(max!=g)){
h+=(g-b)/_238;
}
if((max==g)&&(max!=b)){
h+=(2+(b-r)/_238);
}
if((max==b)&&(max!=r)){
h+=(4+(r-g)/_238);
}
h*=60;
}
h=(h==0)?360:Math.ceil((h/360)*255);
s=Math.ceil(s*255);
l=Math.ceil(l*255);
return [h,s,l];
};
dojo.gfx.color.hsl2rgb=function(h,s,l){
if(dojo.lang.isArray(h)){
l=h[2]||0;
s=h[1]||0;
h=h[0]||0;
}
h=(h/255)*360;
if(h==360){
h=0;
}
s=s/255;
l=l/255;
while(h<0){
h+=360;
}
while(h>360){
h-=360;
}
var r,g,b;
if(h<120){
r=(120-h)/60;
g=h/60;
b=0;
}else{
if(h<240){
r=0;
g=(240-h)/60;
b=(h-120)/60;
}else{
r=(h-240)/60;
g=0;
b=(360-h)/60;
}
}
r=Math.min(r,1);
g=Math.min(g,1);
b=Math.min(b,1);
r=2*s*r+(1-s);
g=2*s*g+(1-s);
b=2*s*b+(1-s);
if(l<0.5){
r=l*r;
g=l*g;
b=l*b;
}else{
r=(1-l)*r+2*l-1;
g=(1-l)*g+2*l-1;
b=(1-l)*b+2*l-1;
}
r=Math.ceil(r*255);
g=Math.ceil(g*255);
b=Math.ceil(b*255);
return [r,g,b];
};
dojo.gfx.color.hsl2hex=function(h,s,l){
var rgb=dojo.gfx.color.hsl2rgb(h,s,l);
return dojo.gfx.color.rgb2hex(rgb[0],rgb[1],rgb[2]);
};
dojo.gfx.color.hex2hsl=function(hex){
var rgb=dojo.gfx.color.hex2rgb(hex);
return dojo.gfx.color.rgb2hsl(rgb[0],rgb[1],rgb[2]);
};
dojo.provide("dojo.charting.*");
dojo.provide("dojo.dom");
dojo.dom.ELEMENT_NODE=1;
dojo.dom.ATTRIBUTE_NODE=2;
dojo.dom.TEXT_NODE=3;
dojo.dom.CDATA_SECTION_NODE=4;
dojo.dom.ENTITY_REFERENCE_NODE=5;
dojo.dom.ENTITY_NODE=6;
dojo.dom.PROCESSING_INSTRUCTION_NODE=7;
dojo.dom.COMMENT_NODE=8;
dojo.dom.DOCUMENT_NODE=9;
dojo.dom.DOCUMENT_TYPE_NODE=10;
dojo.dom.DOCUMENT_FRAGMENT_NODE=11;
dojo.dom.NOTATION_NODE=12;
dojo.dom.dojoml="http://www.dojotoolkit.org/2004/dojoml";
dojo.dom.xmlns={svg:"http://www.w3.org/2000/svg",smil:"http://www.w3.org/2001/SMIL20/",mml:"http://www.w3.org/1998/Math/MathML",cml:"http://www.xml-cml.org",xlink:"http://www.w3.org/1999/xlink",xhtml:"http://www.w3.org/1999/xhtml",xul:"http://www.mozilla.org/keymaster/gatekeeper/there.is.only.xul",xbl:"http://www.mozilla.org/xbl",fo:"http://www.w3.org/1999/XSL/Format",xsl:"http://www.w3.org/1999/XSL/Transform",xslt:"http://www.w3.org/1999/XSL/Transform",xi:"http://www.w3.org/2001/XInclude",xforms:"http://www.w3.org/2002/01/xforms",saxon:"http://icl.com/saxon",xalan:"http://xml.apache.org/xslt",xsd:"http://www.w3.org/2001/XMLSchema",dt:"http://www.w3.org/2001/XMLSchema-datatypes",xsi:"http://www.w3.org/2001/XMLSchema-instance",rdf:"http://www.w3.org/1999/02/22-rdf-syntax-ns#",rdfs:"http://www.w3.org/2000/01/rdf-schema#",dc:"http://purl.org/dc/elements/1.1/",dcq:"http://purl.org/dc/qualifiers/1.0","soap-env":"http://schemas.xmlsoap.org/soap/envelope/",wsdl:"http://schemas.xmlsoap.org/wsdl/",AdobeExtensions:"http://ns.adobe.com/AdobeSVGViewerExtensions/3.0/"};
dojo.dom.isNode=function(wh){
if(typeof Element=="function"){
try{
return wh instanceof Element;
}
catch(e){
}
}else{
return wh&&!isNaN(wh.nodeType);
}
};
dojo.dom.getUniqueId=function(){
var _246=dojo.doc();
do{
var id="dj_unique_"+(++arguments.callee._idIncrement);
}while(_246.getElementById(id));
return id;
};
dojo.dom.getUniqueId._idIncrement=0;
dojo.dom.firstElement=dojo.dom.getFirstChildElement=function(_248,_249){
var node=_248.firstChild;
while(node&&node.nodeType!=dojo.dom.ELEMENT_NODE){
node=node.nextSibling;
}
if(_249&&node&&node.tagName&&node.tagName.toLowerCase()!=_249.toLowerCase()){
node=dojo.dom.nextElement(node,_249);
}
return node;
};
dojo.dom.lastElement=dojo.dom.getLastChildElement=function(_24b,_24c){
var node=_24b.lastChild;
while(node&&node.nodeType!=dojo.dom.ELEMENT_NODE){
node=node.previousSibling;
}
if(_24c&&node&&node.tagName&&node.tagName.toLowerCase()!=_24c.toLowerCase()){
node=dojo.dom.prevElement(node,_24c);
}
return node;
};
dojo.dom.nextElement=dojo.dom.getNextSiblingElement=function(node,_24f){
if(!node){
return null;
}
do{
node=node.nextSibling;
}while(node&&node.nodeType!=dojo.dom.ELEMENT_NODE);
if(node&&_24f&&_24f.toLowerCase()!=node.tagName.toLowerCase()){
return dojo.dom.nextElement(node,_24f);
}
return node;
};
dojo.dom.prevElement=dojo.dom.getPreviousSiblingElement=function(node,_251){
if(!node){
return null;
}
if(_251){
_251=_251.toLowerCase();
}
do{
node=node.previousSibling;
}while(node&&node.nodeType!=dojo.dom.ELEMENT_NODE);
if(node&&_251&&_251.toLowerCase()!=node.tagName.toLowerCase()){
return dojo.dom.prevElement(node,_251);
}
return node;
};
dojo.dom.moveChildren=function(_252,_253,trim){
var _255=0;
if(trim){
while(_252.hasChildNodes()&&_252.firstChild.nodeType==dojo.dom.TEXT_NODE){
_252.removeChild(_252.firstChild);
}
while(_252.hasChildNodes()&&_252.lastChild.nodeType==dojo.dom.TEXT_NODE){
_252.removeChild(_252.lastChild);
}
}
while(_252.hasChildNodes()){
_253.appendChild(_252.firstChild);
_255++;
}
return _255;
};
dojo.dom.copyChildren=function(_256,_257,trim){
var _259=_256.cloneNode(true);
return this.moveChildren(_259,_257,trim);
};
dojo.dom.replaceChildren=function(node,_25b){
var _25c=[];
if(dojo.render.html.ie){
for(var i=0;i<node.childNodes.length;i++){
_25c.push(node.childNodes[i]);
}
}
dojo.dom.removeChildren(node);
node.appendChild(_25b);
for(var i=0;i<_25c.length;i++){
dojo.dom.destroyNode(_25c[i]);
}
};
dojo.dom.removeChildren=function(node){
var _25f=node.childNodes.length;
while(node.hasChildNodes()){
dojo.dom.removeNode(node.firstChild);
}
return _25f;
};
dojo.dom.replaceNode=function(node,_261){
return node.parentNode.replaceChild(_261,node);
};
dojo.dom.destroyNode=function(node){
if(node.parentNode){
node=dojo.dom.removeNode(node);
}
if(node.nodeType!=3){
if(dojo.exists("dojo.event.browser.clean")){
dojo.event.browser.clean(node);
}
if(dojo.render.html.ie){
node.outerHTML="";
}
}
};
dojo.dom.removeNode=function(node){
if(node&&node.parentNode){
return node.parentNode.removeChild(node);
}
};
dojo.dom.getAncestors=function(node,_265,_266){
var _267=[];
var _268=(_265&&(_265 instanceof Function||typeof _265=="function"));
while(node){
if(!_268||_265(node)){
_267.push(node);
}
if(_266&&_267.length>0){
return _267[0];
}
node=node.parentNode;
}
if(_266){
return null;
}
return _267;
};
dojo.dom.getAncestorsByTag=function(node,tag,_26b){
tag=tag.toLowerCase();
return dojo.dom.getAncestors(node,function(el){
return ((el.tagName)&&(el.tagName.toLowerCase()==tag));
},_26b);
};
dojo.dom.getFirstAncestorByTag=function(node,tag){
return dojo.dom.getAncestorsByTag(node,tag,true);
};
dojo.dom.isDescendantOf=function(node,_270,_271){
if(_271&&node){
node=node.parentNode;
}
while(node){
if(node==_270){
return true;
}
node=node.parentNode;
}
return false;
};
dojo.dom.innerXML=function(node){
if(node.innerXML){
return node.innerXML;
}else{
if(node.xml){
return node.xml;
}else{
if(typeof XMLSerializer!="undefined"){
return (new XMLSerializer()).serializeToString(node);
}
}
}
};
dojo.dom.createDocument=function(){
var doc=null;
var _274=dojo.doc();
if(!dj_undef("ActiveXObject")){
var _275=["MSXML2","Microsoft","MSXML","MSXML3"];
for(var i=0;i<_275.length;i++){
try{
doc=new ActiveXObject(_275[i]+".XMLDOM");
}
catch(e){
}
if(doc){
break;
}
}
}else{
if((_274.implementation)&&(_274.implementation.createDocument)){
doc=_274.implementation.createDocument("","",null);
}
}
return doc;
};
dojo.dom.createDocumentFromText=function(str,_278){
if(!_278){
_278="text/xml";
}
if(!dj_undef("DOMParser")){
var _279=new DOMParser();
return _279.parseFromString(str,_278);
}else{
if(!dj_undef("ActiveXObject")){
var _27a=dojo.dom.createDocument();
if(_27a){
_27a.async=false;
_27a.loadXML(str);
return _27a;
}else{
dojo.debug("toXml didn't work?");
}
}else{
var _27b=dojo.doc();
if(_27b.createElement){
var tmp=_27b.createElement("xml");
tmp.innerHTML=str;
if(_27b.implementation&&_27b.implementation.createDocument){
var _27d=_27b.implementation.createDocument("foo","",null);
for(var i=0;i<tmp.childNodes.length;i++){
_27d.importNode(tmp.childNodes.item(i),true);
}
return _27d;
}
return ((tmp.document)&&(tmp.document.firstChild?tmp.document.firstChild:tmp));
}
}
}
return null;
};
dojo.dom.prependChild=function(node,_280){
if(_280.firstChild){
_280.insertBefore(node,_280.firstChild);
}else{
_280.appendChild(node);
}
return true;
};
dojo.dom.insertBefore=function(node,ref,_283){
if((_283!=true)&&(node===ref||node.nextSibling===ref)){
return false;
}
var _284=ref.parentNode;
_284.insertBefore(node,ref);
return true;
};
dojo.dom.insertAfter=function(node,ref,_287){
var pn=ref.parentNode;
if(ref==pn.lastChild){
if((_287!=true)&&(node===ref)){
return false;
}
pn.appendChild(node);
}else{
return this.insertBefore(node,ref.nextSibling,_287);
}
return true;
};
dojo.dom.insertAtPosition=function(node,ref,_28b){
if((!node)||(!ref)||(!_28b)){
return false;
}
switch(_28b.toLowerCase()){
case "before":
return dojo.dom.insertBefore(node,ref);
case "after":
return dojo.dom.insertAfter(node,ref);
case "first":
if(ref.firstChild){
return dojo.dom.insertBefore(node,ref.firstChild);
}else{
ref.appendChild(node);
return true;
}
break;
default:
ref.appendChild(node);
return true;
}
};
dojo.dom.insertAtIndex=function(node,_28d,_28e){
var _28f=_28d.childNodes;
if(!_28f.length||_28f.length==_28e){
_28d.appendChild(node);
return true;
}
if(_28e==0){
return dojo.dom.prependChild(node,_28d);
}
return dojo.dom.insertAfter(node,_28f[_28e-1]);
};
dojo.dom.textContent=function(node,text){
if(arguments.length>1){
var _292=dojo.doc();
dojo.dom.replaceChildren(node,_292.createTextNode(text));
return text;
}else{
if(node["textContent"]!=undefined){
return node.textContent;
}
var _293="";
if(node==null){
return _293;
}
var i=0,n;
while(n=node.childNodes[i++]){
switch(n.nodeType){
case 1:
case 5:
_293+=dojo.dom.textContent(n);
break;
case 3:
case 2:
case 4:
_293+=n.nodeValue;
break;
default:
break;
}
}
return _293;
}
};
dojo.dom.hasParent=function(node){
return Boolean(node&&node.parentNode&&dojo.dom.isNode(node.parentNode));
};
dojo.dom.isTag=function(node){
if(node&&node.tagName){
for(var i=1;i<arguments.length;i++){
if(node.tagName==String(arguments[i])){
return String(arguments[i]);
}
}
}
return "";
};
dojo.dom.setAttributeNS=function(elem,_29a,_29b,_29c){
if(elem==null||((elem==undefined)&&(typeof elem=="undefined"))){
dojo.raise("No element given to dojo.dom.setAttributeNS");
}
if(!((elem.setAttributeNS==undefined)&&(typeof elem.setAttributeNS=="undefined"))){
elem.setAttributeNS(_29a,_29b,_29c);
}else{
var _29d=elem.ownerDocument;
var _29e=_29d.createNode(2,_29b,_29a);
_29e.nodeValue=_29c;
elem.setAttributeNode(_29e);
}
};
dojo.provide("dojo.svg");
dojo.mixin(dojo.svg,dojo.dom);
dojo.svg.graphics=dojo.svg.g=new function(d){
this.suspend=function(){
try{
d.documentElement.suspendRedraw(0);
}
catch(e){
}
};
this.resume=function(){
try{
d.documentElement.unsuspendRedraw(0);
}
catch(e){
}
};
this.force=function(){
try{
d.documentElement.forceRedraw();
}
catch(e){
}
};
}(document);
dojo.svg.animations=dojo.svg.anim=new function(d){
this.arePaused=function(){
try{
return d.documentElement.animationsPaused();
}
catch(e){
return false;
}
};
this.pause=function(){
try{
d.documentElement.pauseAnimations();
}
catch(e){
}
};
this.resume=function(){
try{
d.documentElement.unpauseAnimations();
}
catch(e){
}
};
}(document);
dojo.svg.toCamelCase=function(_2a1){
var arr=_2a1.split("-"),cc=arr[0];
for(var i=1;i<arr.length;i++){
cc+=arr[i].charAt(0).toUpperCase()+arr[i].substring(1);
}
return cc;
};
dojo.svg.toSelectorCase=function(_2a5){
return _2a5.replace(/([A-Z])/g,"-$1").toLowerCase();
};
dojo.svg.getStyle=function(node,_2a7){
return document.defaultView.getComputedStyle(node,_2a7);
};
dojo.svg.getNumericStyle=function(node,_2a9){
return parseFloat(dojo.svg.getStyle(node,_2a9));
};
dojo.svg.getOpacity=function(node){
return Math.min(1,dojo.svg.getNumericStyle(node,"fill-opacity"));
};
dojo.svg.setOpacity=function(node,_2ac){
node.setAttributeNS(this.xmlns.svg,"fill-opacity",_2ac);
node.setAttributeNS(this.xmlns.svg,"stroke-opacity",_2ac);
};
dojo.svg.clearOpacity=function(node){
node.setAttributeNS(this.xmlns.svg,"fill-opacity","1.0");
node.setAttributeNS(this.xmlns.svg,"stroke-opacity","1.0");
};
dojo.svg.getCoords=function(node){
if(node.getBBox){
var box=node.getBBox();
return {x:box.x,y:box.y};
}
return null;
};
dojo.svg.setCoords=function(node,_2b1){
var p=dojo.svg.getCoords();
if(!p){
return;
}
var dx=p.x-_2b1.x;
var dy=p.y-_2b1.y;
dojo.svg.translate(node,dx,dy);
};
dojo.svg.getDimensions=function(node){
if(node.getBBox){
var box=node.getBBox();
return {width:box.width,height:box.height};
}
return null;
};
dojo.svg.setDimensions=function(node,dim){
if(node.width){
node.width.baseVal.value=dim.width;
node.height.baseVal.value=dim.height;
}else{
if(node.r){
node.r.baseVal.value=Math.min(dim.width,dim.height)/2;
}else{
if(node.rx){
node.rx.baseVal.value=dim.width/2;
node.ry.baseVal.value=dim.height/2;
}
}
}
};
dojo.svg.translate=function(node,dx,dy){
if(node.transform&&node.ownerSVGElement&&node.ownerSVGElement.createSVGTransform){
var t=node.ownerSVGElement.createSVGTransform();
t.setTranslate(dx,dy);
node.transform.baseVal.appendItem(t);
}
};
dojo.svg.scale=function(node,_2be,_2bf){
if(!_2bf){
var _2bf=_2be;
}
if(node.transform&&node.ownerSVGElement&&node.ownerSVGElement.createSVGTransform){
var t=node.ownerSVGElement.createSVGTransform();
t.setScale(_2be,_2bf);
node.transform.baseVal.appendItem(t);
}
};
dojo.svg.rotate=function(node,ang,cx,cy){
if(node.transform&&node.ownerSVGElement&&node.ownerSVGElement.createSVGTransform){
var t=node.ownerSVGElement.createSVGTransform();
if(cx==null){
t.setMatrix(t.matrix.rotate(ang));
}else{
t.setRotate(ang,cx,cy);
}
node.transform.baseVal.appendItem(t);
}
};
dojo.svg.skew=function(node,ang,axis){
var dir=axis||"x";
if(node.transform&&node.ownerSVGElement&&node.ownerSVGElement.createSVGTransform){
var t=node.ownerSVGElement.createSVGTransform();
if(dir!="x"){
t.setSkewY(ang);
}else{
t.setSkewX(ang);
}
node.transform.baseVal.appendItem(t);
}
};
dojo.svg.flip=function(node,axis){
var dir=axis||"x";
if(node.transform&&node.ownerSVGElement&&node.ownerSVGElement.createSVGTransform){
var t=node.ownerSVGElement.createSVGTransform();
t.setMatrix((dir!="x")?t.matrix.flipY():t.matrix.flipX());
node.transform.baseVal.appendItem(t);
}
};
dojo.svg.invert=function(node){
if(node.transform&&node.ownerSVGElement&&node.ownerSVGElement.createSVGTransform){
var t=node.ownerSVGElement.createSVGTransform();
t.setMatrix(t.matrix.inverse());
node.transform.baseVal.appendItem(t);
}
};
dojo.svg.applyMatrix=function(node,a,b,c,d,e,f){
if(node.transform&&node.ownerSVGElement&&node.ownerSVGElement.createSVGTransform){
var m;
if(b){
var m=node.ownerSVGElement.createSVGMatrix();
m.a=a;
m.b=b;
m.c=c;
m.d=d;
m.e=e;
m.f=f;
}else{
m=a;
}
var t=node.ownerSVGElement.createSVGTransform();
t.setMatrix(m);
node.transform.baseVal.appendItem(t);
}
};
dojo.svg.group=function(_2da){
var p=_2da.item(0).parentNode;
var g=document.createElementNS(this.xmlns.svg,"g");
for(var i=0;i<_2da.length;i++){
g.appendChild(_2da.item(i));
}
p.appendChild(g);
return g;
};
dojo.svg.ungroup=function(g){
var p=g.parentNode;
while(g.childNodes.length>0){
p.appendChild(g.childNodes.item(0));
}
p.removeChild(g);
};
dojo.svg.getGroup=function(node){
var a=this.getAncestors(node);
for(var i=0;i<a.length;i++){
if(a[i].nodeType==this.ELEMENT_NODE&&a[i].nodeName.toLowerCase()=="g"){
return a[i];
}
}
return null;
};
dojo.svg.bringToFront=function(node){
var n=this.getGroup(node)||node;
n.ownerSVGElement.appendChild(n);
};
dojo.svg.sendToBack=function(node){
var n=this.getGroup(node)||node;
n.ownerSVGElement.insertBefore(n,n.ownerSVGElement.firstChild);
};
dojo.svg.bringForward=function(node){
var n=this.getGroup(node)||node;
if(this.getLastChildElement(n.parentNode)!=n){
this.insertAfter(n,this.getNextSiblingElement(n),true);
}
};
dojo.svg.sendBackward=function(node){
var n=this.getGroup(node)||node;
if(this.getFirstChildElement(n.parentNode)!=n){
this.insertBefore(n,this.getPreviousSiblingElement(n),true);
}
};
dojo.svg.createNodesFromText=function(txt,wrap){
var _2ed=(new DOMParser()).parseFromString(txt,"text/xml").normalize();
if(wrap){
return [_2ed.firstChild.cloneNode(true)];
}
var _2ee=[];
for(var x=0;x<_2ed.childNodes.length;x++){
_2ee.push(_2ed.childNodes.item(x).cloneNode(true));
}
return _2ee;
};
dojo.provide("dojo.charting.Axis");
dojo.charting.Axis=function(_2f0,_2f1,_2f2){
var id="dojo-charting-axis-"+dojo.charting.Axis.count++;
this.getId=function(){
return id;
};
this.setId=function(key){
id=key;
};
this.scale=_2f1||"linear";
this.label=_2f0||"";
this.showLabel=true;
this.showLabels=true;
this.showLines=false;
this.showTicks=false;
this.range={upper:100,lower:0};
this.origin="min";
this._origin=null;
this.labels=_2f2||[];
this._labels=[];
this.nodes={main:null,axis:null,label:null,labels:null,lines:null,ticks:null};
this._rerender=false;
};
dojo.charting.Axis.count=0;
dojo.extend(dojo.charting.Axis,{getCoord:function(val,_2f6,plot){
val=parseFloat(val,10);
var area=_2f6.getArea();
if(plot.axisX==this){
var _2f9=0-this.range.lower;
var min=this.range.lower+_2f9;
var max=this.range.upper+_2f9;
val+=_2f9;
return (val*((area.right-area.left)/max))+area.left;
}else{
var max=this.range.upper;
var min=this.range.lower;
var _2f9=0;
if(min<0){
_2f9+=Math.abs(min);
}
max+=_2f9;
min+=_2f9;
val+=_2f9;
var pmin=area.bottom;
var pmax=area.top;
return (((pmin-pmax)/(max-min))*(max-val))+pmax;
}
},initializeOrigin:function(_2fe,_2ff){
if(this._origin==null){
this._origin=this.origin;
}
if(isNaN(this._origin)){
if(this._origin.toLowerCase()=="max"){
this.origin=_2fe.range[(_2ff=="y")?"upper":"lower"];
}else{
if(this._origin.toLowerCase()=="min"){
this.origin=_2fe.range[(_2ff=="y")?"lower":"upper"];
}else{
this.origin=0;
}
}
}
},initializeLabels:function(){
this._labels=[];
if(this.labels.length==0){
this.showLabels=false;
this.showLines=false;
this.showTicks=false;
}else{
if(this.labels[0].label&&this.labels[0].value!=null){
for(var i=0;i<this.labels.length;i++){
this._labels.push(this.labels[i]);
}
}else{
if(!isNaN(this.labels[0])){
for(var i=0;i<this.labels.length;i++){
this._labels.push({label:this.labels[i],value:this.labels[i]});
}
}else{
var a=[];
for(var i=0;i<this.labels.length;i++){
a.push(this.labels[i]);
}
var s=a.shift();
this._labels.push({label:s,value:this.range.lower});
if(a.length>0){
var s=a.pop();
this._labels.push({label:s,value:this.range.upper});
}
if(a.length>0){
var _303=this.range.upper-this.range.lower;
var step=_303/(this.labels.length-1);
for(var i=1;i<=a.length;i++){
this._labels.push({label:a[i-1],value:this.range.lower+(step*i)});
}
}
}
}
}
},initialize:function(_305,plot,_307,_308){
this.destroy();
this.initializeOrigin(_307,_308);
this.initializeLabels();
var node=this.render(_305,plot,_307,_308);
return node;
},destroy:function(){
for(var p in this.nodes){
while(this.nodes[p]&&this.nodes[p].childNodes.length>0){
this.nodes[p].removeChild(this.nodes[p].childNodes[0]);
}
if(this.nodes[p]&&this.nodes[p].parentNode){
this.nodes[p].parentNode.removeChild(this.nodes[p]);
}
this.nodes[p]=null;
}
}});
dojo.provide("dojo.charting.svg.Axis");
if(dojo.render.svg.capable){
dojo.extend(dojo.charting.Axis,{renderLines:function(_30b,plot,_30d){
if(this.nodes.lines){
while(this.nodes.lines.childNodes.length>0){
this.nodes.lines.removeChild(this.nodes.lines.childNodes[0]);
}
if(this.nodes.lines.parentNode){
this.nodes.lines.parentNode.removeChild(this.nodes.lines);
this.nodes.lines=null;
}
}
var area=_30b.getArea();
var g=this.nodes.lines=document.createElementNS(dojo.svg.xmlns.svg,"g");
g.setAttribute("id",this.getId()+"-lines");
for(var i=0;i<this._labels.length;i++){
if(this._labels[i].value==this.origin){
continue;
}
var v=this.getCoord(this._labels[i].value,_30b,plot);
var l=document.createElementNS(dojo.svg.xmlns.svg,"line");
l.setAttribute("style","stroke:#999;stroke-width:1px;stroke-dasharray:1,4;");
if(_30d=="x"){
l.setAttribute("y1",area.top);
l.setAttribute("y2",area.bottom);
l.setAttribute("x1",v);
l.setAttribute("x2",v);
}else{
if(_30d=="y"){
l.setAttribute("y1",v);
l.setAttribute("y2",v);
l.setAttribute("x1",area.left);
l.setAttribute("x2",area.right);
}
}
g.appendChild(l);
}
return g;
},renderTicks:function(_313,plot,_315,_316){
if(this.nodes.ticks){
while(this.nodes.ticks.childNodes.length>0){
this.nodes.ticks.removeChild(this.nodes.ticks.childNodes[0]);
}
if(this.nodes.ticks.parentNode){
this.nodes.ticks.parentNode.removeChild(this.nodes.ticks);
this.nodes.ticks=null;
}
}
var g=this.nodes.ticks=document.createElementNS(dojo.svg.xmlns.svg,"g");
g.setAttribute("id",this.getId()+"-ticks");
for(var i=0;i<this._labels.length;i++){
var v=this.getCoord(this._labels[i].value,_313,plot);
var l=document.createElementNS(dojo.svg.xmlns.svg,"line");
l.setAttribute("style","stroke:#000;stroke-width:1pt;");
if(_315=="x"){
l.setAttribute("y1",_316);
l.setAttribute("y2",_316+3);
l.setAttribute("x1",v);
l.setAttribute("x2",v);
}else{
if(_315=="y"){
l.setAttribute("y1",v);
l.setAttribute("y2",v);
l.setAttribute("x1",_316-2);
l.setAttribute("x2",_316+2);
}
}
g.appendChild(l);
}
return g;
},renderLabels:function(_31b,plot,_31d,_31e,_31f,_320){
function createLabel(_321,x,y,_324,_325,_326){
var text=document.createElementNS(dojo.svg.xmlns.svg,"text");
text.setAttribute("x",x);
text.setAttribute("y",(_31d=="x"?y:y+2));
text.setAttribute("style","text-anchor:"+_325+";font-family:sans-serif;font-size:"+_324+"px;fill:#000;");
if(_326!=0){
text.setAttribute("transform","rotate("+_326+", "+x+", "+y+")");
}
text.appendChild(document.createTextNode(_321));
return text;
}
if(this.nodes.labels){
while(this.nodes.labels.childNodes.length>0){
this.nodes.labels.removeChild(this.nodes.labels.childNodes[0]);
}
if(this.nodes.labels.parentNode){
this.nodes.labels.parentNode.removeChild(this.nodes.labels);
this.nodes.labels=null;
}
}
var g=this.nodes.labels=document.createElementNS(dojo.svg.xmlns.svg,"g");
g.setAttribute("id",this.getId()+"-labels");
for(var i=0;i<this._labels.length;i++){
var v=this.getCoord(this._labels[i].value,_31b,plot);
if(_31d=="x"){
var d=0;
var _32c=0;
if(this.rotate){
_32c=this.rotate;
d=Math.round(this._labels[i].label.length/2*(_31f/2));
}
var l=createLabel(this._labels[i].label,v,_31e+d,_31f,_320,_32c);
if(this._labels[i].title){
l.title=this._labels[i].title;
var te=document.createElement("div");
te.innerHTML=this._labels[i].title;
te.style.zIndex=50;
te.style.position="absolute";
te.style.border="1px black solid";
te.style.backgroundColor="#fcfdb6";
te.style.visibility="hidden";
document.body.appendChild(te);
l.te=te;
l.onmousemove=function(e){
var src=(typeof window.event!="unefined")?e.target:window.event.srcElement;
if(src.te){
if(src.te.style.visiblity=="visible"){
return;
}else{
src.te.style.visibility="visible";
var lx=0;
var ly=0;
if(!e){
var e=window.event;
}
if(e.pageX||e.pageY){
lx=e.pageX;
ly=e.pageY;
}else{
if(e.clientX||e.clientY){
lx=e.clientX;
ly=e.clientY;
}
}
src.te.style.top=ly+"px";
src.te.style.left=lx+"px";
}
}
};
l.onmouseout=function(e){
var src=(typeof window.event!="unefined")?e.target:window.event.srcElement;
if(src.te){
setTimeout(function(){
src.te.style.visibility="hidden";
},5000);
}
};
}
g.appendChild(l);
}else{
if(_31d=="y"){
g.appendChild(createLabel(this._labels[i].label,_31e,v,_31f,_320));
}
}
}
return g;
},render:function(_335,plot,_337,_338){
if(!this._rerender&&this.nodes.main){
return this.nodes.main;
}
this._rerender=false;
var area=_335.getArea();
var _33a=1;
var _33b="stroke:#000;stroke-width:"+_33a+"px;";
var _33c=10;
var _33d=_337.getCoord(this.origin,_335,plot);
this.nodes.main=document.createElementNS(dojo.svg.xmlns.svg,"g");
var g=this.nodes.main;
g.setAttribute("id",this.getId());
var line=this.nodes.axis=document.createElementNS(dojo.svg.xmlns.svg,"line");
if(_338=="x"){
line.setAttribute("y1",_33d);
line.setAttribute("y2",_33d);
line.setAttribute("x1",area.left-_33a);
line.setAttribute("x2",area.right+_33a);
line.setAttribute("style",_33b);
var y=_33d+_33c+2;
if(this.showLines){
g.appendChild(this.renderLines(_335,plot,_338,y));
}
if(this.showTicks){
g.appendChild(this.renderTicks(_335,plot,_338,_33d));
}
if(this.showLabels){
g.appendChild(this.renderLabels(_335,plot,_338,y,_33c,"middle"));
}
if(this.showLabel&&this.label){
var x=_335.size.width/2;
var text=document.createElementNS(dojo.svg.xmlns.svg,"text");
text.setAttribute("x",x);
if(this.rotate){
text.setAttribute("y",(_335.size.height-_33c));
}else{
text.setAttribute("y",(_33d+(_33c*2)+(_33c/2)));
}
text.setAttribute("style","text-anchor:middle;font-family:sans-serif;font-weight:bold;font-size:"+(_33c+2)+"px;fill:#000;");
text.appendChild(document.createTextNode(this.label));
g.appendChild(text);
}
}else{
line.setAttribute("x1",_33d);
line.setAttribute("x2",_33d);
line.setAttribute("y1",area.top);
line.setAttribute("y2",area.bottom);
line.setAttribute("style",_33b);
var _343=this.origin==_337.range.upper;
var x=_33d+(_343?4:-4);
var _344=_343?"start":"end";
if(this.showLines){
g.appendChild(this.renderLines(_335,plot,_338,x));
}
if(this.showTicks){
g.appendChild(this.renderTicks(_335,plot,_338,_33d));
}
if(this.showLabels){
g.appendChild(this.renderLabels(_335,plot,_338,x,_33c,_344));
}
if(this.showLabel&&this.label){
var x=_343?(_33d+(_33c*2)+(_33c/2)):(_33d-(_33c*4));
var y=_335.size.height/2;
var text=document.createElementNS(dojo.svg.xmlns.svg,"text");
text.setAttribute("x",x);
text.setAttribute("y",y);
text.setAttribute("transform","rotate(90, "+x+", "+y+")");
text.setAttribute("style","text-anchor:middle;font-family:sans-serif;font-weight:bold;font-size:"+(_33c+2)+"px;fill:#000;");
text.appendChild(document.createTextNode(this.label));
g.appendChild(text);
}
}
g.appendChild(line);
return g;
}});
}
dojo.provide("dojo.charting.vml.Axis");
if(dojo.render.vml.capable){
dojo.extend(dojo.charting.Axis,{renderLines:function(_345,plot,_347){
if(this.nodes.lines){
while(this.nodes.lines.childNodes.length>0){
this.nodes.lines.removeChild(this.nodes.lines.childNodes[0]);
}
if(this.nodes.lines.parentNode){
this.nodes.lines.parentNode.removeChild(this.nodes.lines);
this.nodes.lines=null;
}
}
var area=_345.getArea();
var g=this.nodes.lines=document.createElement("div");
g.setAttribute("id",this.getId()+"-lines");
for(var i=0;i<this._labels.length;i++){
if(this._labels[i].value==this.origin){
continue;
}
var v=this.getCoord(this._labels[i].value,_345,plot);
var l=document.createElement("v:line");
var str=document.createElement("v:stroke");
str.dashstyle="dot";
l.appendChild(str);
l.setAttribute("strokecolor","#666");
l.setAttribute("strokeweight","1px");
var s=l.style;
s.position="absolute";
s.top="0px";
s.left="0px";
s.antialias="false";
if(_347=="x"){
l.setAttribute("from",v+"px,"+area.top+"px");
l.setAttribute("to",v+"px,"+area.bottom+"px");
}else{
if(_347=="y"){
l.setAttribute("from",area.left+"px,"+v+"px");
l.setAttribute("to",area.right+"px,"+v+"px");
}
}
g.appendChild(l);
}
return g;
},renderTicks:function(_34f,plot,_351,_352){
if(this.nodes.ticks){
while(this.nodes.ticks.childNodes.length>0){
this.nodes.ticks.removeChild(this.nodes.ticks.childNodes[0]);
}
if(this.nodes.ticks.parentNode){
this.nodes.ticks.parentNode.removeChild(this.nodes.ticks);
this.nodes.ticks=null;
}
}
var g=this.nodes.ticks=document.createElement("div");
g.setAttribute("id",this.getId()+"-ticks");
for(var i=0;i<this._labels.length;i++){
var v=this.getCoord(this._labels[i].value,_34f,plot);
var l=document.createElement("v:line");
l.setAttribute("strokecolor","#000");
l.setAttribute("strokeweight","1px");
var s=l.style;
s.position="absolute";
s.top="0px";
s.left="0px";
s.antialias="false";
if(_351=="x"){
l.setAttribute("from",v+"px,"+_352+"px");
l.setAttribute("to",v+"px,"+(_352+3)+"px");
}else{
if(_351=="y"){
l.setAttribute("from",(_352-2)+"px,"+v+"px");
l.setAttribute("to",(_352+2)+"px,"+v+"px");
}
}
g.appendChild(l);
}
return g;
},renderLabels:function(_358,plot,_35a,_35b,_35c,_35d){
function createLabel(_35e,x,y,_361,_362,_363){
var text=document.createElement("div");
var s=text.style;
text.innerHTML=_35e;
s.fontSize=_361+"px";
s.fontFamily="sans-serif";
s.position="absolute";
if(_363!=0&&_35a=="x"){
s.writingMode="tb-rl";
}
s.top=y+"px";
if(_362=="center"){
s.left=x+"px";
s.textAlign="center";
}else{
if(_362=="left"){
s.left=x+"px";
s.textAlign="left";
}else{
if(_362=="right"){
s.right=x+"px";
s.textAlign="right";
}
}
}
return text;
}
if(this.nodes.labels){
while(this.nodes.labels.childNodes.length>0){
this.nodes.labels.removeChild(this.nodes.labels.childNodes[0]);
}
if(this.nodes.labels.parentNode){
this.nodes.labels.parentNode.removeChild(this.nodes.labels);
this.nodes.labels=null;
}
}
var g=this.nodes.labels=document.createElement("div");
g.setAttribute("id",this.getId()+"-labels");
for(var i=0;i<this._labels.length;i++){
var v=this.getCoord(this._labels[i].value,_358,plot);
if(_35a=="x"){
var node=createLabel(this._labels[i].label,v,_35b,_35c,_35d,this.rotate);
if(this._labels[i].title){
node.title=this.labels[i].title;
node.style.cursor="pointer";
}
document.body.appendChild(node);
node.style.left=v-(node.offsetWidth/2)+"px";
g.appendChild(node);
}else{
if(_35a=="y"){
var node=createLabel(this._labels[i].label,_35b,v,_35c,_35d,0);
document.body.appendChild(node);
node.style.top=v-(node.offsetHeight/2)+"px";
g.appendChild(node);
}
}
}
return g;
},render:function(_36a,plot,_36c,_36d){
if(!this._rerender&&this.nodes.main){
return this.nodes.main;
}
this._rerender=false;
var area=_36a.getArea();
var _36f=1;
var _370="stroke:#000;stroke-width:"+_36f+"px;";
var _371=10;
var _372=_36c.getCoord(this.origin,_36a,plot);
var g=this.nodes.main=document.createElement("div");
g.setAttribute("id",this.getId());
var line=this.nodes.axis=document.createElement("v:line");
line.setAttribute("strokecolor","#000");
line.setAttribute("strokeweight",_36f+"px");
var s=line.style;
s.position="absolute";
s.top="0px";
s.left="0px";
s.antialias="false";
if(_36d=="x"){
line.setAttribute("from",area.left+"px,"+_372+"px");
line.setAttribute("to",area.right+"px,"+_372+"px");
var y=_372+Math.floor(_371/2);
if(this.showLines){
g.appendChild(this.renderLines(_36a,plot,_36d,y));
}
if(this.showTicks){
g.appendChild(this.renderTicks(_36a,plot,_36d,_372));
}
if(this.showLabels){
var _377=0;
if(this.rotate){
_377=this.rotate;
}
g.appendChild(this.renderLabels(_36a,plot,_36d,y,_371,"center",_377));
}
if(this.showLabel&&this.label){
var x=_36a.size.width/2;
var y=_372+Math.round(_371*1.5);
var text=document.createElement("div");
var s=text.style;
text.innerHTML=this.label;
s.fontSize=(_371+2)+"px";
s.fontFamily="sans-serif";
s.fontWeight="bold";
s.position="absolute";
if(this.rotate){
s.top=(_36a.size.height-_371)-2+"px";
}else{
s.top=y+"px";
}
s.left=x+"px";
s.textAlign="center";
document.body.appendChild(text);
text.style.left=x-(text.offsetWidth/2)+"px";
g.appendChild(text);
}
}else{
line.setAttribute("from",_372+"px,"+area.top+"px");
line.setAttribute("to",_372+"px,"+area.bottom+"px");
var _37a=this.origin==_36c.range.upper;
var x=_372+4;
var _37b="left";
if(!_37a){
x=area.right-_372+_371+4;
_37b="right";
if(_372==area.left){
x+=(_371*2)-(_371/2);
}
}
if(this.showLines){
g.appendChild(this.renderLines(_36a,plot,_36d,x));
}
if(this.showTicks){
g.appendChild(this.renderTicks(_36a,plot,_36d,_372));
}
if(this.showLabels){
g.appendChild(this.renderLabels(_36a,plot,_36d,x,_371,_37b,0));
}
if(this.showLabel&&this.label){
x+=(_371*2)-2;
var y=_36a.size.height/2;
var text=document.createElement("div");
var s=text.style;
text.innerHTML=this.label;
s.fontSize=(_371+2)+"px";
s.fontFamily="sans-serif";
s.fontWeight="bold";
s.position="absolute";
s.height=_36a.size.height+"px";
s.writingMode="tb-rl";
s.textAlign="center";
s[_37b]=x+"px";
document.body.appendChild(text);
s.top=y-(text.offsetHeight/2)+"px";
g.appendChild(text);
}
}
g.appendChild(line);
return g;
}});
}
dojo.provide("dojo.charting.Plotters");
dojo.provide("dojo.charting.svg.Plotters");
if(dojo.render.svg.capable){
dojo.mixin(dojo.charting.Plotters,{Bar:function(_37c,plot,_37e,_37f){
var area=_37c.getArea();
var _381=document.createElementNS(dojo.svg.xmlns.svg,"g");
var n=plot.series.length;
var data=[];
for(var i=0;i<n;i++){
var tmp=plot.series[i].data.evaluate(_37e);
data.push(tmp);
}
var _386=8;
var _387=data[0].length;
if(_387==0){
return _381;
}
var _388=((area.right-area.left)-(_386*(_387-1)))/_387;
var _389=_388/n;
var _38a=plot.axisY.getCoord(plot.axisX.origin,_37c,plot);
for(var i=0;i<_387;i++){
var _38b=area.left+(_388*i)+(_386*i);
for(var j=0;j<n;j++){
var _38d=data[j][i].y;
var yA=_38a;
var x=_38b+(_389*j);
var y=plot.axisY.getCoord(_38d,_37c,plot);
var h=Math.abs(yA-y);
if(_38d<plot.axisX.origin){
yA=y;
y=_38a;
}
var bar=document.createElementNS(dojo.svg.xmlns.svg,"rect");
bar.setAttribute("fill",data[j][i].series.color);
bar.setAttribute("stroke-width","0");
bar.setAttribute("x",x);
bar.setAttribute("y",y);
bar.setAttribute("width",_389);
bar.setAttribute("height",h);
bar.setAttribute("fill-opacity","0.6");
if(_37f){
_37f(bar,data[j][i].src);
}
_381.appendChild(bar);
}
}
return _381;
},HorizontalBar:function(_393,plot,_395,_396){
var area=_393.getArea();
var _398=document.createElementNS(dojo.svg.xmlns.svg,"g");
var n=plot.series.length;
var data=[];
for(var i=0;i<n;i++){
var tmp=plot.series[i].data.evaluate(_395);
data.push(tmp);
}
var _39d=6;
var _39e=data[0].length;
if(_39e==0){
return _398;
}
var h=((area.bottom-area.top)-(_39d*(_39e-1)))/_39e;
var barH=h/n;
var _3a1=plot.axisX.getCoord(0,_393,plot);
for(var i=0;i<_39e;i++){
var _3a2=area.top+(h*i)+(_39d*i);
for(var j=0;j<n;j++){
var _3a4=data[j][i].y;
var y=_3a2+(barH*j);
var xA=_3a1;
var x=plot.axisX.getCoord(_3a4,_393,plot);
var w=Math.abs(x-xA);
if(_3a4>0){
x=_3a1;
}
var bar=document.createElementNS(dojo.svg.xmlns.svg,"rect");
bar.setAttribute("fill",data[j][i].series.color);
bar.setAttribute("stroke-width","0");
bar.setAttribute("x",xA);
bar.setAttribute("y",y);
bar.setAttribute("width",w);
bar.setAttribute("height",barH);
bar.setAttribute("fill-opacity","0.6");
if(_396){
_396(bar,data[j][i].src);
}
_398.appendChild(bar);
}
}
return _398;
},Gantt:function(_3aa,plot,_3ac,_3ad){
var area=_3aa.getArea();
var _3af=document.createElementNS(dojo.svg.xmlns.svg,"g");
var n=plot.series.length;
var data=[];
for(var i=0;i<n;i++){
var tmp=plot.series[i].data.evaluate(_3ac);
data.push(tmp);
}
var _3b4=2;
var _3b5=data[0].length;
if(_3b5==0){
return _3af;
}
var h=((area.bottom-area.top)-(_3b4*(_3b5-1)))/_3b5;
var barH=h/n;
for(var i=0;i<_3b5;i++){
var _3b8=area.top+(h*i)+(_3b4*i);
for(var j=0;j<n;j++){
var high=data[j][i].high;
var low=data[j][i].low;
if(low>high){
var t=high;
high=low;
low=t;
}
var x=plot.axisX.getCoord(low,_3aa,plot);
var w=plot.axisX.getCoord(high,_3aa,plot)-x;
var y=_3b8+(barH*j);
var bar=document.createElementNS(dojo.svg.xmlns.svg,"rect");
bar.setAttribute("fill",data[j][i].series.color);
bar.setAttribute("stroke-width","0");
bar.setAttribute("x",x);
bar.setAttribute("y",y);
bar.setAttribute("width",w);
bar.setAttribute("height",barH);
bar.setAttribute("fill-opacity","0.6");
if(_3ad){
_3ad(bar,data[j][i].src);
}
_3af.appendChild(bar);
}
}
return _3af;
},StackedArea:function(_3c1,plot,_3c3,_3c4){
var area=_3c1.getArea();
var _3c6=document.createElementNS(dojo.svg.xmlns.svg,"g");
var n=plot.series.length;
var data=[];
var _3c9=[];
for(var i=0;i<n;i++){
var tmp=plot.series[i].data.evaluate(_3c3);
for(var j=0;j<tmp.length;j++){
if(i==0){
_3c9.push(tmp[j].y);
}else{
_3c9[j]+=tmp[j].y;
}
tmp[j].y=_3c9[j];
}
data.push(tmp);
}
for(var i=n-1;i>=0;i--){
var path=document.createElementNS(dojo.svg.xmlns.svg,"path");
path.setAttribute("fill",data[i][0].series.color);
path.setAttribute("fill-opacity","0.4");
path.setAttribute("stroke",data[i][0].series.color);
path.setAttribute("stroke-width","1");
path.setAttribute("stroke-opacity","0.85");
var cmd=[];
var r=3;
for(var j=0;j<data[i].length;j++){
var _3d0=data[i];
var x=plot.axisX.getCoord(_3d0[j].x,_3c1,plot);
var y=plot.axisY.getCoord(_3d0[j].y,_3c1,plot);
if(j==0){
cmd.push("M");
}else{
cmd.push("L");
}
cmd.push(x+","+y);
var c=document.createElementNS(dojo.svg.xmlns.svg,"circle");
c.setAttribute("cx",x);
c.setAttribute("cy",y);
c.setAttribute("r","3");
c.setAttribute("fill",_3d0[j].series.color);
c.setAttribute("fill-opacity","0.6");
c.setAttribute("stroke-width","1");
c.setAttribute("stroke-opacity","0.85");
_3c6.appendChild(c);
if(_3c4){
_3c4(c,data[i].src);
}
}
if(i==0){
cmd.push("L");
cmd.push(x+","+plot.axisY.getCoord(plot.axisX.origin,_3c1,plot));
cmd.push("L");
cmd.push(plot.axisX.getCoord(data[0][0].x,_3c1,plot)+","+plot.axisY.getCoord(plot.axisX.origin,_3c1,plot));
cmd.push("Z");
}else{
var _3d0=data[i-1];
cmd.push("L");
cmd.push(x+","+Math.round(plot.axisY.getCoord(_3d0[_3d0.length-1].y,_3c1,plot)));
for(var j=_3d0.length-2;j>=0;j--){
var x=plot.axisX.getCoord(_3d0[j].x,_3c1,plot);
var y=plot.axisY.getCoord(_3d0[j].y,_3c1,plot);
cmd.push("L");
cmd.push(x+","+y);
}
}
path.setAttribute("d",cmd.join(" ")+" Z");
_3c6.appendChild(path);
}
return _3c6;
},StackedCurvedArea:function(_3d4,plot,_3d6,_3d7){
var _3d8=3;
var area=_3d4.getArea();
var _3da=document.createElementNS(dojo.svg.xmlns.svg,"g");
var n=plot.series.length;
var data=[];
var _3dd=[];
for(var i=0;i<n;i++){
var tmp=plot.series[i].data.evaluate(_3d6);
for(var j=0;j<tmp.length;j++){
if(i==0){
_3dd.push(tmp[j].y);
}else{
_3dd[j]+=tmp[j].y;
}
tmp[j].y=_3dd[j];
}
data.push(tmp);
}
for(var i=n-1;i>=0;i--){
var path=document.createElementNS(dojo.svg.xmlns.svg,"path");
path.setAttribute("fill",data[i][0].series.color);
path.setAttribute("fill-opacity","0.4");
path.setAttribute("stroke",data[i][0].series.color);
path.setAttribute("stroke-width","1");
path.setAttribute("stroke-opacity","0.85");
var cmd=[];
var r=3;
for(var j=0;j<data[i].length;j++){
var _3e4=data[i];
var x=plot.axisX.getCoord(_3e4[j].x,_3d4,plot);
var y=plot.axisY.getCoord(_3e4[j].y,_3d4,plot);
var dx=area.left+1;
var dy=area.bottom;
if(j>0){
dx=x-plot.axisX.getCoord(_3e4[j-1].x,_3d4,plot);
dy=plot.axisY.getCoord(_3e4[j-1].y,_3d4,plot);
}
if(j==0){
cmd.push("M");
}else{
cmd.push("C");
var cx=x-(_3d8-1)*(dx/_3d8);
cmd.push(cx+","+dy);
cx=x-(dx/_3d8);
cmd.push(cx+","+y);
}
cmd.push(x+","+y);
var c=document.createElementNS(dojo.svg.xmlns.svg,"circle");
c.setAttribute("cx",x);
c.setAttribute("cy",y);
c.setAttribute("r","3");
c.setAttribute("fill",_3e4[j].series.color);
c.setAttribute("fill-opacity","0.6");
c.setAttribute("stroke-width","1");
c.setAttribute("stroke-opacity","0.85");
_3da.appendChild(c);
if(_3d7){
_3d7(c,data[i].src);
}
}
if(i==0){
cmd.push("L");
cmd.push(x+","+plot.axisY.getCoord(plot.axisX.origin,_3d4,plot));
cmd.push("L");
cmd.push(plot.axisX.getCoord(data[0][0].x,_3d4,plot)+","+plot.axisY.getCoord(plot.axisX.origin,_3d4,plot));
cmd.push("Z");
}else{
var _3e4=data[i-1];
cmd.push("L");
cmd.push(x+","+Math.round(plot.axisY.getCoord(_3e4[_3e4.length-1].y,_3d4,plot)));
for(var j=_3e4.length-2;j>=0;j--){
var x=plot.axisX.getCoord(_3e4[j].x,_3d4,plot);
var y=plot.axisY.getCoord(_3e4[j].y,_3d4,plot);
var dx=x-plot.axisX.getCoord(_3e4[j+1].x,_3d4,plot);
var dy=plot.axisY.getCoord(_3e4[j+1].y,_3d4,plot);
cmd.push("C");
var cx=x-(_3d8-1)*(dx/_3d8);
cmd.push(cx+","+dy);
cx=x-(dx/_3d8);
cmd.push(cx+","+y);
cmd.push(x+","+y);
}
}
path.setAttribute("d",cmd.join(" ")+" Z");
_3da.appendChild(path);
}
return _3da;
},DataBar:function(data,_3ec,plot,_3ee){
var area=_3ec.getArea();
var _3f0=document.createElementNS(dojo.svg.xmlns.svg,"g");
var n=data.length;
var w=(area.right-area.left)/(plot.axisX.range.upper-plot.axisX.range.lower);
var _3f3=plot.axisY.getCoord(plot.axisX.origin,_3ec,plot);
for(var i=0;i<n;i++){
var _3f5=data[i].y;
var yA=_3f3;
var x=plot.axisX.getCoord(data[i].x,_3ec,plot)-(w/2);
var y=plot.axisY.getCoord(_3f5,_3ec,plot);
var h=Math.abs(yA-y);
if(_3f5<plot.axisX.origin){
yA=y;
y=_3f3;
}
var bar=document.createElementNS(dojo.svg.xmlns.svg,"rect");
bar.setAttribute("fill",data[i].series.color);
bar.setAttribute("stroke-width","0");
bar.setAttribute("x",x);
bar.setAttribute("y",y);
bar.setAttribute("width",w);
bar.setAttribute("height",h);
bar.setAttribute("fill-opacity","0.6");
if(_3ee){
_3ee(bar,data[i].src);
}
_3f0.appendChild(bar);
}
return _3f0;
},Line:function(data,_3fc,plot,_3fe){
var area=_3fc.getArea();
var line=document.createElementNS(dojo.svg.xmlns.svg,"g");
if(data.length==0){
return line;
}
var path=document.createElementNS(dojo.svg.xmlns.svg,"path");
line.appendChild(path);
path.setAttribute("fill","none");
path.setAttribute("stroke",data[0].series.color);
path.setAttribute("stroke-width","2");
path.setAttribute("stroke-opacity","0.85");
if(data[0].series.label!=null){
path.setAttribute("title",data[0].series.label);
}
var cmd=[];
for(var i=0;i<data.length;i++){
var x=plot.axisX.getCoord(data[i].x,_3fc,plot);
var y=plot.axisY.getCoord(data[i].y,_3fc,plot);
if(i==0){
cmd.push("M");
}else{
cmd.push("L");
}
cmd.push(x+","+y);
var c=document.createElementNS(dojo.svg.xmlns.svg,"circle");
c.setAttribute("cx",x);
c.setAttribute("cy",y);
c.setAttribute("r","3");
c.setAttribute("fill",data[i].series.color);
c.setAttribute("fill-opacity","0.6");
c.setAttribute("stroke-width","1");
c.setAttribute("stroke-opacity","0.85");
line.appendChild(c);
if(_3fe){
_3fe(c,data[i].src);
}
}
path.setAttribute("d",cmd.join(" "));
return line;
},CurvedLine:function(data,_408,plot,_40a){
var _40b=3;
var area=_408.getArea();
var line=document.createElementNS(dojo.svg.xmlns.svg,"g");
if(data.length==0){
return line;
}
var path=document.createElementNS(dojo.svg.xmlns.svg,"path");
line.appendChild(path);
path.setAttribute("fill","none");
path.setAttribute("stroke",data[0].series.color);
path.setAttribute("stroke-width","2");
path.setAttribute("stroke-opacity","0.85");
if(data[0].series.label!=null){
path.setAttribute("title",data[0].series.label);
}
var cmd=[];
for(var i=0;i<data.length;i++){
var x=plot.axisX.getCoord(data[i].x,_408,plot);
var y=plot.axisY.getCoord(data[i].y,_408,plot);
var dx=area.left+1;
var dy=area.bottom;
if(i>0){
dx=x-plot.axisX.getCoord(data[i-1].x,_408,plot);
dy=plot.axisY.getCoord(data[i-1].y,_408,plot);
}
if(i==0){
cmd.push("M");
}else{
cmd.push("C");
var cx=x-(_40b-1)*(dx/_40b);
cmd.push(cx+","+dy);
cx=x-(dx/_40b);
cmd.push(cx+","+y);
}
cmd.push(x+","+y);
var c=document.createElementNS(dojo.svg.xmlns.svg,"circle");
c.setAttribute("cx",x);
c.setAttribute("cy",y);
c.setAttribute("r","3");
c.setAttribute("fill",data[i].series.color);
c.setAttribute("fill-opacity","0.6");
c.setAttribute("stroke-width","1");
c.setAttribute("stroke-opacity","0.85");
line.appendChild(c);
if(_40a){
_40a(c,data[i].src);
}
}
path.setAttribute("d",cmd.join(" "));
return line;
},Area:function(data,_418,plot,_41a){
var area=_418.getArea();
var line=document.createElementNS(dojo.svg.xmlns.svg,"g");
if(data.length==0){
return line;
}
var path=document.createElementNS(dojo.svg.xmlns.svg,"path");
line.appendChild(path);
path.setAttribute("fill",data[0].series.color);
path.setAttribute("fill-opacity","0.4");
path.setAttribute("stroke",data[0].series.color);
path.setAttribute("stroke-width","1");
path.setAttribute("stroke-opacity","0.85");
if(data[0].series.label!=null){
path.setAttribute("title",data[0].series.label);
}
var cmd=[];
for(var i=0;i<data.length;i++){
var x=plot.axisX.getCoord(data[i].x,_418,plot);
var y=plot.axisY.getCoord(data[i].y,_418,plot);
if(i==0){
cmd.push("M");
}else{
cmd.push("L");
}
cmd.push(x+","+y);
var c=document.createElementNS(dojo.svg.xmlns.svg,"circle");
c.setAttribute("cx",x);
c.setAttribute("cy",y);
c.setAttribute("r","3");
c.setAttribute("fill",data[i].series.color);
c.setAttribute("fill-opacity","0.6");
c.setAttribute("stroke-width","1");
c.setAttribute("stroke-opacity","0.85");
line.appendChild(c);
if(_41a){
_41a(c,data[i].src);
}
}
cmd.push("L");
cmd.push(x+","+plot.axisY.getCoord(plot.axisX.origin,_418,plot));
cmd.push("L");
cmd.push(plot.axisX.getCoord(data[0].x,_418,plot)+","+plot.axisY.getCoord(plot.axisX.origin,_418,plot));
cmd.push("Z");
path.setAttribute("d",cmd.join(" "));
return line;
},CurvedArea:function(data,_424,plot,_426){
var _427=3;
var area=_424.getArea();
var line=document.createElementNS(dojo.svg.xmlns.svg,"g");
if(data.length==0){
return line;
}
var path=document.createElementNS(dojo.svg.xmlns.svg,"path");
line.appendChild(path);
path.setAttribute("fill",data[0].series.color);
path.setAttribute("fill-opacity","0.4");
path.setAttribute("stroke",data[0].series.color);
path.setAttribute("stroke-width","1");
path.setAttribute("stroke-opacity","0.85");
if(data[0].series.label!=null){
path.setAttribute("title",data[0].series.label);
}
var cmd=[];
for(var i=0;i<data.length;i++){
var x=plot.axisX.getCoord(data[i].x,_424,plot);
var y=plot.axisY.getCoord(data[i].y,_424,plot);
var dx=area.left+1;
var dy=area.bottom;
if(i>0){
dx=x-plot.axisX.getCoord(data[i-1].x,_424,plot);
dy=plot.axisY.getCoord(data[i-1].y,_424,plot);
}
if(i==0){
cmd.push("M");
}else{
cmd.push("C");
var cx=x-(_427-1)*(dx/_427);
cmd.push(cx+","+dy);
cx=x-(dx/_427);
cmd.push(cx+","+y);
}
cmd.push(x+","+y);
var c=document.createElementNS(dojo.svg.xmlns.svg,"circle");
c.setAttribute("cx",x);
c.setAttribute("cy",y);
c.setAttribute("r","3");
c.setAttribute("fill",data[i].series.color);
c.setAttribute("fill-opacity","0.6");
c.setAttribute("stroke-width","1");
c.setAttribute("stroke-opacity","0.85");
line.appendChild(c);
if(_426){
_426(c,data[i].src);
}
}
cmd.push("L");
cmd.push(x+","+plot.axisY.getCoord(plot.axisX.origin,_424,plot));
cmd.push("L");
cmd.push(plot.axisX.getCoord(data[0].x,_424,plot)+","+plot.axisY.getCoord(plot.axisX.origin,_424,plot));
cmd.push("Z");
path.setAttribute("d",cmd.join(" "));
return line;
},HighLow:function(data,_434,plot,_436){
var area=_434.getArea();
var _438=document.createElementNS(dojo.svg.xmlns.svg,"g");
var n=data.length;
var part=((area.right-area.left)/(plot.axisX.range.upper-plot.axisX.range.lower))/4;
var w=part*2;
for(var i=0;i<n;i++){
var high=data[i].high;
var low=data[i].low;
if(low>high){
var t=low;
low=high;
high=t;
}
var x=plot.axisX.getCoord(data[i].x,_434,plot)-(w/2);
var y=plot.axisY.getCoord(high,_434,plot);
var h=plot.axisY.getCoord(low,_434,plot)-y;
var bar=document.createElementNS(dojo.svg.xmlns.svg,"rect");
bar.setAttribute("fill",data[i].series.color);
bar.setAttribute("stroke-width","0");
bar.setAttribute("x",x);
bar.setAttribute("y",y);
bar.setAttribute("width",w);
bar.setAttribute("height",h);
bar.setAttribute("fill-opacity","0.6");
if(_436){
_436(bar,data[i].src);
}
_438.appendChild(bar);
}
return _438;
},HighLowClose:function(data,_445,plot,_447){
var area=_445.getArea();
var _449=document.createElementNS(dojo.svg.xmlns.svg,"g");
var n=data.length;
var part=((area.right-area.left)/(plot.axisX.range.upper-plot.axisX.range.lower))/4;
var w=part*2;
for(var i=0;i<n;i++){
var high=data[i].high;
var low=data[i].low;
if(low>high){
var t=low;
low=high;
high=t;
}
var c=data[i].close;
var x=plot.axisX.getCoord(data[i].x,_445,plot)-(w/2);
var y=plot.axisY.getCoord(high,_445,plot);
var h=plot.axisY.getCoord(low,_445,plot)-y;
var _455=plot.axisY.getCoord(c,_445,plot);
var g=document.createElementNS(dojo.svg.xmlns.svg,"g");
var bar=document.createElementNS(dojo.svg.xmlns.svg,"rect");
bar.setAttribute("fill",data[i].series.color);
bar.setAttribute("stroke-width","0");
bar.setAttribute("x",x);
bar.setAttribute("y",y);
bar.setAttribute("width",w);
bar.setAttribute("height",h);
bar.setAttribute("fill-opacity","0.6");
g.appendChild(bar);
var line=document.createElementNS(dojo.svg.xmlns.svg,"line");
line.setAttribute("x1",x);
line.setAttribute("x2",x+w+(part*2));
line.setAttribute("y1",_455);
line.setAttribute("y2",_455);
line.setAttribute("style","stroke:"+data[i].series.color+";stroke-width:1px;stroke-opacity:0.6;");
g.appendChild(line);
if(_447){
_447(g,data[i].src);
}
_449.appendChild(g);
}
return _449;
},HighLowOpenClose:function(data,_45a,plot,_45c){
var area=_45a.getArea();
var _45e=document.createElementNS(dojo.svg.xmlns.svg,"g");
var n=data.length;
var part=((area.right-area.left)/(plot.axisX.range.upper-plot.axisX.range.lower))/4;
var w=part*2;
for(var i=0;i<n;i++){
var high=data[i].high;
var low=data[i].low;
if(low>high){
var t=low;
low=high;
high=t;
}
var o=data[i].open;
var c=data[i].close;
var x=plot.axisX.getCoord(data[i].x,_45a,plot)-(w/2);
var y=plot.axisY.getCoord(high,_45a,plot);
var h=plot.axisY.getCoord(low,_45a,plot)-y;
var open=plot.axisY.getCoord(o,_45a,plot);
var _46c=plot.axisY.getCoord(c,_45a,plot);
var g=document.createElementNS(dojo.svg.xmlns.svg,"g");
var bar=document.createElementNS(dojo.svg.xmlns.svg,"rect");
bar.setAttribute("fill",data[i].series.color);
bar.setAttribute("stroke-width","0");
bar.setAttribute("x",x);
bar.setAttribute("y",y);
bar.setAttribute("width",w);
bar.setAttribute("height",h);
bar.setAttribute("fill-opacity","0.6");
g.appendChild(bar);
var line=document.createElementNS(dojo.svg.xmlns.svg,"line");
line.setAttribute("x1",x-(part*2));
line.setAttribute("x2",x+w);
line.setAttribute("y1",open);
line.setAttribute("y2",open);
line.setAttribute("style","stroke:"+data[i].series.color+";stroke-width:1px;stroke-opacity:0.6;");
g.appendChild(line);
var line=document.createElementNS(dojo.svg.xmlns.svg,"line");
line.setAttribute("x1",x);
line.setAttribute("x2",x+w+(part*2));
line.setAttribute("y1",_46c);
line.setAttribute("y2",_46c);
line.setAttribute("style","stroke:"+data[i].series.color+";stroke-width:1px;stroke-opacity:0.6;");
g.appendChild(line);
if(_45c){
_45c(g,data[i].src);
}
_45e.appendChild(g);
}
return _45e;
},Scatter:function(data,_471,plot,_473){
var r=7;
var _475=document.createElementNS(dojo.svg.xmlns.svg,"g");
for(var i=0;i<data.length;i++){
var x=plot.axisX.getCoord(data[i].x,_471,plot);
var y=plot.axisY.getCoord(data[i].y,_471,plot);
var _479=document.createElementNS(dojo.svg.xmlns.svg,"path");
_479.setAttribute("fill",data[i].series.color);
_479.setAttribute("stroke-width","0");
_479.setAttribute("d","M "+x+","+(y-r)+" "+"Q "+x+","+y+" "+(x+r)+","+y+" "+"Q "+x+","+y+" "+x+","+(y+r)+" "+"Q "+x+","+y+" "+(x-r)+","+y+" "+"Q "+x+","+y+" "+x+","+(y-r)+" "+"Z");
if(_473){
_473(_479,data[i].src);
}
_475.appendChild(_479);
}
return _475;
},Bubble:function(data,_47b,plot,_47d){
var _47e=document.createElementNS(dojo.svg.xmlns.svg,"g");
var _47f=1;
for(var i=0;i<data.length;i++){
var x=plot.axisX.getCoord(data[i].x,_47b,plot);
var y=plot.axisY.getCoord(data[i].y,_47b,plot);
if(i==0){
var raw=data[i].size;
var dy=plot.axisY.getCoord(data[i].y+raw,_47b,plot)-y;
_47f=dy/raw;
}
if(_47f<1){
_47f=1;
}
var _485=document.createElementNS(dojo.svg.xmlns.svg,"circle");
_485.setAttribute("fill",data[i].series.color);
_485.setAttribute("fill-opacity","0.8");
_485.setAttribute("stroke",data[i].series.color);
_485.setAttribute("stroke-width","1");
_485.setAttribute("cx",x);
_485.setAttribute("cy",y);
_485.setAttribute("r",(data[i].size/2)*_47f);
if(_47d){
_47d(_485,data[i].src);
}
_47e.appendChild(_485);
}
return _47e;
}});
dojo.charting.Plotters["Default"]=dojo.charting.Plotters.Line;
}
dojo.provide("dojo.charting.vml.Plotters");
if(dojo.render.vml.capable){
dojo.mixin(dojo.charting.Plotters,{_group:function(_486){
var _487=document.createElement("div");
_487.style.position="absolute";
_487.style.top="0px";
_487.style.left="0px";
_487.style.width=_486.size.width+"px";
_487.style.height=_486.size.height+"px";
return _487;
},Bar:function(_488,plot,_48a,_48b){
var area=_488.getArea();
var _48d=dojo.charting.Plotters._group(_488);
var n=plot.series.length;
var data=[];
for(var i=0;i<n;i++){
var tmp=plot.series[i].data.evaluate(_48a);
data.push(tmp);
}
var _492=8;
var _493=data[0].length;
if(_493==0){
return _48d;
}
var _494=((area.right-area.left)-(_492*(_493-1)))/_493;
var _495=Math.round(_494/n);
var _496=plot.axisY.getCoord(plot.axisX.origin,_488,plot);
for(var i=0;i<_493;i++){
var _497=area.left+(_494*i)+(_492*i);
for(var j=0;j<n;j++){
var _499=data[j][i].y;
var yA=_496;
var x=_497+(_495*j);
var y=plot.axisY.getCoord(_499,_488,plot);
var h=Math.abs(yA-y);
if(_499<plot.axisX.origin){
yA=y;
y=_496;
}
var bar=document.createElement("v:rect");
bar.style.position="absolute";
bar.style.top=y+1+"px";
bar.style.left=x+"px";
bar.style.width=_495+"px";
bar.style.height=h+"px";
bar.setAttribute("fillColor",data[j][i].series.color);
bar.setAttribute("stroked","false");
bar.style.antialias="false";
var fill=document.createElement("v:fill");
fill.setAttribute("opacity","0.6");
bar.appendChild(fill);
if(_48b){
_48b(bar,data[j][i].src);
}
_48d.appendChild(bar);
}
}
return _48d;
},HorizontalBar:function(_4a0,plot,_4a2,_4a3){
var area=_4a0.getArea();
var _4a5=dojo.charting.Plotters._group(_4a0);
var n=plot.series.length;
var data=[];
for(var i=0;i<n;i++){
var tmp=plot.series[i].data.evaluate(_4a2);
data.push(tmp);
}
var _4aa=6;
var _4ab=data[0].length;
if(_4ab==0){
return _4a5;
}
var h=((area.bottom-area.top)-(_4aa*(_4ab-1)))/_4ab;
var barH=h/n;
var _4ae=plot.axisX.getCoord(0,_4a0,plot);
for(var i=0;i<_4ab;i++){
var _4af=area.top+(h*i)+(_4aa*i);
for(var j=0;j<n;j++){
var _4b1=data[j][i].y;
var y=_4af+(barH*j);
var xA=_4ae;
var x=plot.axisX.getCoord(_4b1,_4a0,plot);
var w=Math.abs(x-xA);
if(_4b1>0){
x=_4ae;
}
var bar=document.createElement("v:rect");
bar.style.position="absolute";
bar.style.top=y+1+"px";
bar.style.left=xA+"px";
bar.style.width=w+"px";
bar.style.height=barH+"px";
bar.setAttribute("fillColor",data[j][i].series.color);
bar.setAttribute("stroked","false");
bar.style.antialias="false";
var fill=document.createElement("v:fill");
fill.setAttribute("opacity","0.6");
bar.appendChild(fill);
if(_4a3){
_4a3(bar,data[j][i].src);
}
_4a5.appendChild(bar);
}
}
var _4aa=4;
var n=plot.series.length;
var h=((area.bottom-area.top)-(_4aa*(n-1)))/n;
var _4ae=plot.axisX.getCoord(0,_4a0,plot);
for(var i=0;i<n;i++){
var _4b8=plot.series[i];
var data=_4b8.data.evaluate(_4a2);
var y=area.top+(h*i)+(_4aa*i);
var _4b1=data[data.length-1].y;
var xA=_4ae;
var x=plot.axisX.getCoord(_4b1,_4a0,plot);
var w=Math.abs(xA-x);
if(_4b1>0){
xA=x;
x=_4ae;
}
}
return _4a5;
},Gantt:function(_4b9,plot,_4bb,_4bc){
var area=_4b9.getArea();
var _4be=dojo.charting.Plotters._group(_4b9);
var n=plot.series.length;
var data=[];
for(var i=0;i<n;i++){
var tmp=plot.series[i].data.evaluate(_4bb);
data.push(tmp);
}
var _4c3=2;
var _4c4=data[0].length;
if(_4c4==0){
return _4be;
}
var h=((area.bottom-area.top)-(_4c3*(_4c4-1)))/_4c4;
var barH=h/n;
for(var i=0;i<_4c4;i++){
var _4c7=area.top+(h*i)+(_4c3*i);
for(var j=0;j<n;j++){
var high=data[j][i].high;
var low=data[j][i].low;
if(low>high){
var t=high;
high=low;
low=t;
}
var x=plot.axisX.getCoord(low,_4b9,plot);
var w=plot.axisX.getCoord(high,_4b9,plot)-x;
var y=_4c7+(barH*j);
var bar=document.createElement("v:rect");
bar.style.position="absolute";
bar.style.top=y+1+"px";
bar.style.left=x+"px";
bar.style.width=w+"px";
bar.style.height=barH+"px";
bar.setAttribute("fillColor",data[j][i].series.color);
bar.setAttribute("stroked","false");
bar.style.antialias="false";
var fill=document.createElement("v:fill");
fill.setAttribute("opacity","0.6");
bar.appendChild(fill);
if(_4bc){
_4bc(bar,data[j][i].src);
}
_4be.appendChild(bar);
}
}
return _4be;
},StackedArea:function(_4d1,plot,_4d3,_4d4){
var area=_4d1.getArea();
var _4d6=dojo.charting.Plotters._group(_4d1);
var n=plot.series.length;
var data=[];
var _4d9=[];
for(var i=0;i<n;i++){
var tmp=plot.series[i].data.evaluate(_4d3);
for(var j=0;j<tmp.length;j++){
if(i==0){
_4d9.push(tmp[j].y);
}else{
_4d9[j]+=tmp[j].y;
}
tmp[j].y=_4d9[j];
}
data.push(tmp);
}
for(var i=n-1;i>=0;i--){
var path=document.createElement("v:shape");
path.setAttribute("strokeweight","1px");
path.setAttribute("strokecolor",data[i][0].series.color);
path.setAttribute("fillcolor",data[i][0].series.color);
path.setAttribute("coordsize",(area.right-area.left)+","+(area.bottom-area.top));
path.style.position="absolute";
path.style.top="0px";
path.style.left="0px";
path.style.width=area.right-area.left+"px";
path.style.height=area.bottom-area.top+"px";
var _4de=document.createElement("v:stroke");
_4de.setAttribute("opacity","0.8");
path.appendChild(_4de);
var fill=document.createElement("v:fill");
fill.setAttribute("opacity","0.4");
path.appendChild(fill);
var cmd=[];
var r=3;
for(var j=0;j<data[i].length;j++){
var _4e2=data[i];
var x=Math.round(plot.axisX.getCoord(_4e2[j].x,_4d1,plot));
var y=Math.round(plot.axisY.getCoord(_4e2[j].y,_4d1,plot));
if(j==0){
cmd.push("m");
cmd.push(x+","+y);
}else{
cmd.push("l");
cmd.push(x+","+y);
}
var c=document.createElement("v:oval");
c.setAttribute("strokeweight","1px");
c.setAttribute("strokecolor",_4e2[j].series.color);
c.setAttribute("fillcolor",_4e2[j].series.color);
var str=document.createElement("v:stroke");
str.setAttribute("opacity","0.8");
c.appendChild(str);
str=document.createElement("v:fill");
str.setAttribute("opacity","0.6");
c.appendChild(str);
var s=c.style;
s.position="absolute";
s.top=(y-r)+"px";
s.left=(x-r)+"px";
s.width=(r*2)+"px";
s.height=(r*2)+"px";
_4d6.appendChild(c);
if(_4d4){
_4d4(c,data[j].src);
}
}
if(i==0){
cmd.push("l");
cmd.push(x+","+Math.round(plot.axisY.getCoord(plot.axisX.origin,_4d1,plot)));
cmd.push("l");
cmd.push(Math.round(plot.axisX.getCoord(data[0][0].x,_4d1,plot))+","+Math.round(plot.axisY.getCoord(plot.axisX.origin,_4d1,plot)));
}else{
var _4e2=data[i-1];
cmd.push("l");
cmd.push(x+","+Math.round(plot.axisY.getCoord(_4e2[_4e2.length-1].y,_4d1,plot)));
for(var j=_4e2.length-2;j>=0;j--){
var x=Math.round(plot.axisX.getCoord(_4e2[j].x,_4d1,plot));
var y=Math.round(plot.axisY.getCoord(_4e2[j].y,_4d1,plot));
cmd.push("l");
cmd.push(x+","+y);
}
}
path.setAttribute("path",cmd.join(" ")+" x e");
_4d6.appendChild(path);
}
return _4d6;
},StackedCurvedArea:function(_4e8,plot,_4ea,_4eb){
var _4ec=3;
var area=_4e8.getArea();
var _4ee=dojo.charting.Plotters._group(_4e8);
var n=plot.series.length;
var data=[];
var _4f1=[];
for(var i=0;i<n;i++){
var tmp=plot.series[i].data.evaluate(_4ea);
for(var j=0;j<tmp.length;j++){
if(i==0){
_4f1.push(tmp[j].y);
}else{
_4f1[j]+=tmp[j].y;
}
tmp[j].y=_4f1[j];
}
data.push(tmp);
}
for(var i=n-1;i>=0;i--){
var path=document.createElement("v:shape");
path.setAttribute("strokeweight","1px");
path.setAttribute("strokecolor",data[i][0].series.color);
path.setAttribute("fillcolor",data[i][0].series.color);
path.setAttribute("coordsize",(area.right-area.left)+","+(area.bottom-area.top));
path.style.position="absolute";
path.style.top="0px";
path.style.left="0px";
path.style.width=area.right-area.left+"px";
path.style.height=area.bottom-area.top+"px";
var _4f6=document.createElement("v:stroke");
_4f6.setAttribute("opacity","0.8");
path.appendChild(_4f6);
var fill=document.createElement("v:fill");
fill.setAttribute("opacity","0.4");
path.appendChild(fill);
var cmd=[];
var r=3;
for(var j=0;j<data[i].length;j++){
var _4fa=data[i];
var x=Math.round(plot.axisX.getCoord(_4fa[j].x,_4e8,plot));
var y=Math.round(plot.axisY.getCoord(_4fa[j].y,_4e8,plot));
if(j==0){
cmd.push("m");
cmd.push(x+","+y);
}else{
var _4fd=Math.round(plot.axisX.getCoord(_4fa[j-1].x,_4e8,plot));
var _4fe=Math.round(plot.axisY.getCoord(_4fa[j-1].y,_4e8,plot));
var dx=x-_4fd;
var dy=y-_4fe;
cmd.push("c");
var cx=Math.round((x-(_4ec-1)*(dx/_4ec)));
cmd.push(cx+","+_4fe);
cx=Math.round((x-(dx/_4ec)));
cmd.push(cx+","+y);
cmd.push(x+","+y);
}
var c=document.createElement("v:oval");
c.setAttribute("strokeweight","1px");
c.setAttribute("strokecolor",_4fa[j].series.color);
c.setAttribute("fillcolor",_4fa[j].series.color);
var str=document.createElement("v:stroke");
str.setAttribute("opacity","0.8");
c.appendChild(str);
str=document.createElement("v:fill");
str.setAttribute("opacity","0.6");
c.appendChild(str);
var s=c.style;
s.position="absolute";
s.top=(y-r)+"px";
s.left=(x-r)+"px";
s.width=(r*2)+"px";
s.height=(r*2)+"px";
_4ee.appendChild(c);
if(_4eb){
_4eb(c,data[j].src);
}
}
if(i==0){
cmd.push("l");
cmd.push(x+","+Math.round(plot.axisY.getCoord(plot.axisX.origin,_4e8,plot)));
cmd.push("l");
cmd.push(Math.round(plot.axisX.getCoord(data[0][0].x,_4e8,plot))+","+Math.round(plot.axisY.getCoord(plot.axisX.origin,_4e8,plot)));
}else{
var _4fa=data[i-1];
cmd.push("l");
cmd.push(x+","+Math.round(plot.axisY.getCoord(_4fa[_4fa.length-1].y,_4e8,plot)));
for(var j=_4fa.length-2;j>=0;j--){
var x=Math.round(plot.axisX.getCoord(_4fa[j].x,_4e8,plot));
var y=Math.round(plot.axisY.getCoord(_4fa[j].y,_4e8,plot));
var _4fd=Math.round(plot.axisX.getCoord(_4fa[j+1].x,_4e8,plot));
var _4fe=Math.round(plot.axisY.getCoord(_4fa[j+1].y,_4e8,plot));
var dx=x-_4fd;
var dy=y-_4fe;
cmd.push("c");
var cx=Math.round((x-(_4ec-1)*(dx/_4ec)));
cmd.push(cx+","+_4fe);
cx=Math.round((x-(dx/_4ec)));
cmd.push(cx+","+y);
cmd.push(x+","+y);
}
}
path.setAttribute("path",cmd.join(" ")+" x e");
_4ee.appendChild(path);
}
return _4ee;
},DataBar:function(data,_506,plot,_508){
var area=_506.getArea();
var _50a=dojo.charting.Plotters._group(_506);
var n=data.length;
var w=(area.right-area.left)/(plot.axisX.range.upper-plot.axisX.range.lower);
var _50d=plot.axisY.getCoord(plot.axisX.origin,_506,plot);
for(var i=0;i<n;i++){
var _50f=data[i].y;
var yA=_50d;
var x=plot.axisX.getCoord(data[i].x,_506,plot)-(w/2)+1;
var y=plot.axisY.getCoord(_50f,_506,plot);
var h=Math.abs(yA-y);
if(_50f<plot.axisX.origin){
yA=y;
y=_50d;
}
var bar=document.createElement("v:rect");
bar.style.position="absolute";
bar.style.top=y+1+"px";
bar.style.left=x+"px";
bar.style.width=w+"px";
bar.style.height=h+"px";
bar.setAttribute("fillColor",data[i].series.color);
bar.setAttribute("stroked","false");
bar.style.antialias="false";
var fill=document.createElement("v:fill");
fill.setAttribute("opacity","0.6");
bar.appendChild(fill);
if(_508){
_508(bar,data[i].src);
}
_50a.appendChild(bar);
}
return _50a;
},Line:function(data,_517,plot,_519){
var area=_517.getArea();
var _51b=dojo.charting.Plotters._group(_517);
if(data.length==0){
return _51b;
}
var path=document.createElement("v:shape");
path.setAttribute("strokeweight","2px");
path.setAttribute("strokecolor",data[0].series.color);
path.setAttribute("fillcolor","none");
path.setAttribute("filled","false");
path.setAttribute("coordsize",(area.right-area.left)+","+(area.bottom-area.top));
path.style.position="absolute";
path.style.top="0px";
path.style.left="0px";
path.style.width=area.right-area.left+"px";
path.style.height=area.bottom-area.top+"px";
var _51d=document.createElement("v:stroke");
_51d.setAttribute("opacity","0.8");
path.appendChild(_51d);
var cmd=[];
var r=3;
for(var i=0;i<data.length;i++){
var x=Math.round(plot.axisX.getCoord(data[i].x,_517,plot));
var y=Math.round(plot.axisY.getCoord(data[i].y,_517,plot));
if(i==0){
cmd.push("m");
cmd.push(x+","+y);
}else{
cmd.push("l");
cmd.push(x+","+y);
}
var c=document.createElement("v:oval");
c.setAttribute("strokeweight","1px");
c.setAttribute("strokecolor",data[i].series.color);
c.setAttribute("fillcolor",data[i].series.color);
var str=document.createElement("v:stroke");
str.setAttribute("opacity","0.8");
c.appendChild(str);
str=document.createElement("v:fill");
str.setAttribute("opacity","0.6");
c.appendChild(str);
var s=c.style;
s.position="absolute";
s.top=(y-r)+"px";
s.left=(x-r)+"px";
s.width=(r*2)+"px";
s.height=(r*2)+"px";
_51b.appendChild(c);
if(_519){
_519(c,data[i].src);
}
}
path.setAttribute("path",cmd.join(" ")+" e");
_51b.appendChild(path);
return _51b;
},CurvedLine:function(data,_527,plot,_529){
var _52a=3;
var area=_527.getArea();
var _52c=dojo.charting.Plotters._group(_527);
if(data.length==0){
return _52c;
}
var path=document.createElement("v:shape");
path.setAttribute("strokeweight","2px");
path.setAttribute("strokecolor",data[0].series.color);
path.setAttribute("fillcolor","none");
path.setAttribute("filled","false");
path.setAttribute("coordsize",(area.right-area.left)+","+(area.bottom-area.top));
path.style.position="absolute";
path.style.top="0px";
path.style.left="0px";
path.style.width=area.right-area.left+"px";
path.style.height=area.bottom-area.top+"px";
var _52e=document.createElement("v:stroke");
_52e.setAttribute("opacity","0.8");
path.appendChild(_52e);
var cmd=[];
var r=3;
for(var i=0;i<data.length;i++){
var x=Math.round(plot.axisX.getCoord(data[i].x,_527,plot));
var y=Math.round(plot.axisY.getCoord(data[i].y,_527,plot));
if(i==0){
cmd.push("m");
cmd.push(x+","+y);
}else{
var _534=Math.round(plot.axisX.getCoord(data[i-1].x,_527,plot));
var _535=Math.round(plot.axisY.getCoord(data[i-1].y,_527,plot));
var dx=x-_534;
var dy=y-_535;
cmd.push("c");
var cx=Math.round((x-(_52a-1)*(dx/_52a)));
cmd.push(cx+","+_535);
cx=Math.round((x-(dx/_52a)));
cmd.push(cx+","+y);
cmd.push(x+","+y);
}
var c=document.createElement("v:oval");
c.setAttribute("strokeweight","1px");
c.setAttribute("strokecolor",data[i].series.color);
c.setAttribute("fillcolor",data[i].series.color);
var str=document.createElement("v:stroke");
str.setAttribute("opacity","0.8");
c.appendChild(str);
str=document.createElement("v:fill");
str.setAttribute("opacity","0.6");
c.appendChild(str);
var s=c.style;
s.position="absolute";
s.top=(y-r)+"px";
s.left=(x-r)+"px";
s.width=(r*2)+"px";
s.height=(r*2)+"px";
_52c.appendChild(c);
if(_529){
_529(c,data[i].src);
}
}
path.setAttribute("path",cmd.join(" ")+" e");
_52c.appendChild(path);
return _52c;
},Area:function(data,_53d,plot,_53f){
var area=_53d.getArea();
var _541=dojo.charting.Plotters._group(_53d);
if(data.length==0){
return _541;
}
var path=document.createElement("v:shape");
path.setAttribute("strokeweight","1px");
path.setAttribute("strokecolor",data[0].series.color);
path.setAttribute("fillcolor",data[0].series.color);
path.setAttribute("coordsize",(area.right-area.left)+","+(area.bottom-area.top));
path.style.position="absolute";
path.style.top="0px";
path.style.left="0px";
path.style.width=area.right-area.left+"px";
path.style.height=area.bottom-area.top+"px";
var _543=document.createElement("v:stroke");
_543.setAttribute("opacity","0.8");
path.appendChild(_543);
var fill=document.createElement("v:fill");
fill.setAttribute("opacity","0.4");
path.appendChild(fill);
var cmd=[];
var r=3;
for(var i=0;i<data.length;i++){
var x=Math.round(plot.axisX.getCoord(data[i].x,_53d,plot));
var y=Math.round(plot.axisY.getCoord(data[i].y,_53d,plot));
if(i==0){
cmd.push("m");
cmd.push(x+","+y);
}else{
cmd.push("l");
cmd.push(x+","+y);
}
var c=document.createElement("v:oval");
c.setAttribute("strokeweight","1px");
c.setAttribute("strokecolor",data[i].series.color);
c.setAttribute("fillcolor",data[i].series.color);
var str=document.createElement("v:stroke");
str.setAttribute("opacity","0.8");
c.appendChild(str);
str=document.createElement("v:fill");
str.setAttribute("opacity","0.6");
c.appendChild(str);
var s=c.style;
s.position="absolute";
s.top=(y-r)+"px";
s.left=(x-r)+"px";
s.width=(r*2)+"px";
s.height=(r*2)+"px";
_541.appendChild(c);
if(_53f){
_53f(c,data[i].src);
}
}
cmd.push("l");
cmd.push(x+","+Math.round(plot.axisY.getCoord(plot.axisX.origin,_53d,plot)));
cmd.push("l");
cmd.push(Math.round(plot.axisX.getCoord(data[0].x,_53d,plot))+","+Math.round(plot.axisY.getCoord(plot.axisX.origin,_53d,plot)));
path.setAttribute("path",cmd.join(" ")+" x e");
_541.appendChild(path);
return _541;
},CurvedArea:function(data,_54e,plot,_550){
var _551=3;
var area=_54e.getArea();
var _553=dojo.charting.Plotters._group(_54e);
if(data.length==0){
return _553;
}
var path=document.createElement("v:shape");
path.setAttribute("strokeweight","1px");
path.setAttribute("strokecolor",data[0].series.color);
path.setAttribute("fillcolor",data[0].series.color);
path.setAttribute("coordsize",(area.right-area.left)+","+(area.bottom-area.top));
path.style.position="absolute";
path.style.top="0px";
path.style.left="0px";
path.style.width=area.right-area.left+"px";
path.style.height=area.bottom-area.top+"px";
var _555=document.createElement("v:stroke");
_555.setAttribute("opacity","0.8");
path.appendChild(_555);
var fill=document.createElement("v:fill");
fill.setAttribute("opacity","0.4");
path.appendChild(fill);
var cmd=[];
var r=3;
for(var i=0;i<data.length;i++){
var x=Math.round(plot.axisX.getCoord(data[i].x,_54e,plot));
var y=Math.round(plot.axisY.getCoord(data[i].y,_54e,plot));
if(i==0){
cmd.push("m");
cmd.push(x+","+y);
}else{
var _55c=Math.round(plot.axisX.getCoord(data[i-1].x,_54e,plot));
var _55d=Math.round(plot.axisY.getCoord(data[i-1].y,_54e,plot));
var dx=x-_55c;
var dy=y-_55d;
cmd.push("c");
var cx=Math.round((x-(_551-1)*(dx/_551)));
cmd.push(cx+","+_55d);
cx=Math.round((x-(dx/_551)));
cmd.push(cx+","+y);
cmd.push(x+","+y);
}
var c=document.createElement("v:oval");
c.setAttribute("strokeweight","1px");
c.setAttribute("strokecolor",data[i].series.color);
c.setAttribute("fillcolor",data[i].series.color);
var str=document.createElement("v:stroke");
str.setAttribute("opacity","0.8");
c.appendChild(str);
str=document.createElement("v:fill");
str.setAttribute("opacity","0.6");
c.appendChild(str);
var s=c.style;
s.position="absolute";
s.top=(y-r)+"px";
s.left=(x-r)+"px";
s.width=(r*2)+"px";
s.height=(r*2)+"px";
_553.appendChild(c);
if(_550){
_550(c,data[i].src);
}
}
cmd.push("l");
cmd.push(x+","+Math.round(plot.axisY.getCoord(plot.axisX.origin,_54e,plot)));
cmd.push("l");
cmd.push(Math.round(plot.axisX.getCoord(data[0].x,_54e,plot))+","+Math.round(plot.axisY.getCoord(plot.axisX.origin,_54e,plot)));
path.setAttribute("path",cmd.join(" ")+" x e");
_553.appendChild(path);
return _553;
},HighLow:function(data,_565,plot,_567){
var area=_565.getArea();
var _569=dojo.charting.Plotters._group(_565);
var n=data.length;
var part=((area.right-area.left)/(plot.axisX.range.upper-plot.axisX.range.lower))/4;
var w=part*2;
for(var i=0;i<n;i++){
var high=data[i].high;
var low=data[i].low;
if(low>high){
var t=low;
low=high;
high=t;
}
var x=plot.axisX.getCoord(data[i].x,_565,plot)-(w/2);
var y=plot.axisY.getCoord(high,_565,plot);
var h=plot.axisY.getCoord(low,_565,plot)-y;
var bar=document.createElement("v:rect");
bar.style.position="absolute";
bar.style.top=y+1+"px";
bar.style.left=x+"px";
bar.style.width=w+"px";
bar.style.height=h+"px";
bar.setAttribute("fillColor",data[i].series.color);
bar.setAttribute("stroked","false");
bar.style.antialias="false";
var fill=document.createElement("v:fill");
fill.setAttribute("opacity","0.6");
bar.appendChild(fill);
if(_567){
_567(bar,data[i].src);
}
_569.appendChild(bar);
}
return _569;
},HighLowClose:function(data,_577,plot,_579){
var area=_577.getArea();
var _57b=dojo.charting.Plotters._group(_577);
var n=data.length;
var part=((area.right-area.left)/(plot.axisX.range.upper-plot.axisX.range.lower))/4;
var w=part*2;
for(var i=0;i<n;i++){
var high=data[i].high;
var low=data[i].low;
if(low>high){
var t=low;
low=high;
high=t;
}
var c=data[i].close;
var x=plot.axisX.getCoord(data[i].x,_577,plot)-(w/2);
var y=plot.axisY.getCoord(high,_577,plot);
var h=plot.axisY.getCoord(low,_577,plot)-y;
var _587=plot.axisY.getCoord(c,_577,plot);
var g=document.createElement("div");
var bar=document.createElement("v:rect");
bar.style.position="absolute";
bar.style.top=y+1+"px";
bar.style.left=x+"px";
bar.style.width=w+"px";
bar.style.height=h+"px";
bar.setAttribute("fillColor",data[i].series.color);
bar.setAttribute("stroked","false");
bar.style.antialias="false";
var fill=document.createElement("v:fill");
fill.setAttribute("opacity","0.6");
bar.appendChild(fill);
g.appendChild(bar);
var line=document.createElement("v:line");
line.setAttribute("strokecolor",data[i].series.color);
line.setAttribute("strokeweight","1px");
line.setAttribute("from",x+"px,"+_587+"px");
line.setAttribute("to",(x+w+(part*2)-2)+"px,"+_587+"px");
var s=line.style;
s.position="absolute";
s.top="0px";
s.left="0px";
s.antialias="false";
var str=document.createElement("v:stroke");
str.setAttribute("opacity","0.6");
line.appendChild(str);
g.appendChild(line);
if(_579){
_579(g,data[i].src);
}
_57b.appendChild(g);
}
return _57b;
},HighLowOpenClose:function(data,_58f,plot,_591){
var area=_58f.getArea();
var _593=dojo.charting.Plotters._group(_58f);
var n=data.length;
var part=((area.right-area.left)/(plot.axisX.range.upper-plot.axisX.range.lower))/4;
var w=part*2;
for(var i=0;i<n;i++){
var high=data[i].high;
var low=data[i].low;
if(low>high){
var t=low;
low=high;
high=t;
}
var o=data[i].open;
var c=data[i].close;
var x=plot.axisX.getCoord(data[i].x,_58f,plot)-(w/2);
var y=plot.axisY.getCoord(high,_58f,plot);
var h=plot.axisY.getCoord(low,_58f,plot)-y;
var open=plot.axisY.getCoord(o,_58f,plot);
var _5a1=plot.axisY.getCoord(c,_58f,plot);
var g=document.createElement("div");
var bar=document.createElement("v:rect");
bar.style.position="absolute";
bar.style.top=y+1+"px";
bar.style.left=x+"px";
bar.style.width=w+"px";
bar.style.height=h+"px";
bar.setAttribute("fillColor",data[i].series.color);
bar.setAttribute("stroked","false");
bar.style.antialias="false";
var fill=document.createElement("v:fill");
fill.setAttribute("opacity","0.6");
bar.appendChild(fill);
g.appendChild(bar);
var line=document.createElement("v:line");
line.setAttribute("strokecolor",data[i].series.color);
line.setAttribute("strokeweight","1px");
line.setAttribute("from",(x-(part*2))+"px,"+open+"px");
line.setAttribute("to",(x+w-2)+"px,"+open+"px");
var s=line.style;
s.position="absolute";
s.top="0px";
s.left="0px";
s.antialias="false";
var str=document.createElement("v:stroke");
str.setAttribute("opacity","0.6");
line.appendChild(str);
g.appendChild(line);
var line=document.createElement("v:line");
line.setAttribute("strokecolor",data[i].series.color);
line.setAttribute("strokeweight","1px");
line.setAttribute("from",x+"px,"+_5a1+"px");
line.setAttribute("to",(x+w+(part*2)-2)+"px,"+_5a1+"px");
var s=line.style;
s.position="absolute";
s.top="0px";
s.left="0px";
s.antialias="false";
var str=document.createElement("v:stroke");
str.setAttribute("opacity","0.6");
line.appendChild(str);
g.appendChild(line);
if(_591){
_591(g,data[i].src);
}
_593.appendChild(g);
}
return _593;
},Scatter:function(data,_5a9,plot,_5ab){
var r=6;
var mod=r/2;
var area=_5a9.getArea();
var _5af=dojo.charting.Plotters._group(_5a9);
for(var i=0;i<data.length;i++){
var x=Math.round(plot.axisX.getCoord(data[i].x,_5a9,plot));
var y=Math.round(plot.axisY.getCoord(data[i].y,_5a9,plot));
var _5b3=document.createElement("v:rect");
_5b3.setAttribute("strokecolor",data[i].series.color);
_5b3.setAttribute("fillcolor",data[i].series.color);
var fill=document.createElement("v:fill");
fill.setAttribute("opacity","0.6");
_5b3.appendChild(fill);
var s=_5b3.style;
s.position="absolute";
s.rotation="45";
s.top=(y-mod)+"px";
s.left=(x-mod)+"px";
s.width=r+"px";
s.height=r+"px";
_5af.appendChild(_5b3);
if(_5ab){
_5ab(_5b3,data[i].src);
}
}
return _5af;
},Bubble:function(data,_5b7,plot,_5b9){
var _5ba=1;
var area=_5b7.getArea();
var _5bc=dojo.charting.Plotters._group(_5b7);
for(var i=0;i<data.length;i++){
var x=Math.round(plot.axisX.getCoord(data[i].x,_5b7,plot));
var y=Math.round(plot.axisY.getCoord(data[i].y,_5b7,plot));
if(i==0){
var raw=data[i].size;
var dy=plot.axisY.getCoord(data[i].y+raw,_5b7,plot)-y;
_5ba=dy/raw;
}
if(_5ba<1){
_5ba=1;
}
var r=(data[i].size/2)*_5ba;
var _5c3=document.createElement("v:oval");
_5c3.setAttribute("strokecolor",data[i].series.color);
_5c3.setAttribute("fillcolor",data[i].series.color);
var fill=document.createElement("v:fill");
fill.setAttribute("opacity","0.6");
_5c3.appendChild(fill);
var s=_5c3.style;
s.position="absolute";
s.rotation="45";
s.top=(y-r)+"px";
s.left=(x-r)+"px";
s.width=(r*2)+"px";
s.height=(r*2)+"px";
_5bc.appendChild(_5c3);
if(_5b9){
_5b9(_5c3,data[i].src);
}
}
return _5bc;
}});
dojo.charting.Plotters["Default"]=dojo.charting.Plotters.Line;
}
dojo.provide("dojo.charting.Series");
dojo.charting.Series=function(_5c6){
var args=_5c6||{length:1};
this.dataSource=args.dataSource||null;
this.bindings={};
this.color=args.color;
this.label=args.label;
if(args.bindings){
for(var p in args.bindings){
this.addBinding(p,args.bindings[p]);
}
}
};
dojo.extend(dojo.charting.Series,{bind:function(src,_5ca){
this.dataSource=src;
this.bindings=_5ca;
},addBinding:function(name,_5cc){
this.bindings[name]=_5cc;
},evaluate:function(_5cd){
var ret=[];
var a=this.dataSource.getData();
var l=a.length;
var _5d1=0;
var end=l;
if(_5cd){
if(_5cd.between){
for(var i=0;i<l;i++){
var fld=this.dataSource.getField(a[i],_5cd.between.field);
if(fld>=_5cd.between.low&&fld<=_5cd.between.high){
var o={src:a[i],series:this};
for(var p in this.bindings){
o[p]=this.dataSource.getField(a[i],this.bindings[p]);
}
ret.push(o);
}
}
}else{
if(_5cd.from||_5cd.length){
if(_5cd.from){
_5d1=Math.max(_5cd.from,0);
if(_5cd.to){
end=Math.min(_5cd.to,end);
}
}else{
if(_5cd.length<0){
_5d1=Math.max((end+length),0);
}else{
end=Math.min((_5d1+length),end);
}
}
for(var i=_5d1;i<end;i++){
var o={src:a[i],series:this};
for(var p in this.bindings){
o[p]=this.dataSource.getField(a[i],this.bindings[p]);
}
ret.push(o);
}
}
}
}else{
for(var i=_5d1;i<end;i++){
var o={src:a[i],series:this};
for(var p in this.bindings){
o[p]=this.dataSource.getField(a[i],this.bindings[p]);
}
ret.push(o);
}
}
if(ret.length>0&&typeof (ret[0].x)!="undefined"){
ret.sort(function(a,b){
if(a.x>b.x){
return 1;
}
if(a.x<b.x){
return -1;
}
return 0;
});
}
return ret;
},trends:{createRange:function(_5d9,len){
var idx=_5d9.length-1;
var _5dc=(len||_5d9.length);
return {"index":idx,"length":_5dc,"start":Math.max(idx-_5dc,0)};
},mean:function(_5dd,len){
var _5df=this.createRange(_5dd,len);
if(_5df.index<0){
return 0;
}
var _5e0=0;
var _5e1=0;
for(var i=_5df.index;i>=_5df.start;i--){
_5e0+=_5dd[i].y;
_5e1++;
}
_5e0/=Math.max(_5e1,1);
return _5e0;
},variance:function(_5e3,len){
var _5e5=this.createRange(_5e3,len);
if(_5e5.index<0){
return 0;
}
var _5e6=0;
var _5e7=0;
var _5e8=0;
for(var i=_5e5.index;i>=_5e5.start;i--){
_5e6+=_5e3[i].y;
_5e7+=Math.pow(_5e3[i].y,2);
_5e8++;
}
return (_5e7/_5e8)-Math.pow(_5e6/_5e8,2);
},standardDeviation:function(_5ea,len){
return Math.sqrt(this.getVariance(_5ea,len));
},max:function(_5ec,len){
var _5ee=this.createRange(_5ec,len);
if(_5ee.index<0){
return 0;
}
var max=Number.MIN_VALUE;
for(var i=_5ee.index;i>=_5ee.start;i--){
max=Math.max(_5ec[i].y,max);
}
return max;
},min:function(_5f1,len){
var _5f3=this.createRange(_5f1,len);
if(_5f3.index<0){
return 0;
}
var min=Number.MAX_VALUE;
for(var i=_5f3.index;i>=_5f3.start;i--){
min=Math.min(_5f1[i].y,min);
}
return min;
},median:function(_5f6,len){
var _5f8=this.createRange(_5f6,len);
if(_5f8.index<0){
return 0;
}
var a=[];
for(var i=_5f8.index;i>=_5f8.start;i--){
var b=false;
for(var j=0;j<a.length;j++){
if(_5f6[i].y==a[j]){
b=true;
break;
}
}
if(!b){
a.push(_5f6[i].y);
}
}
a.sort();
if(a.length>0){
return a[Math.ceil(a.length/2)];
}
return 0;
},mode:function(_5fd,len){
var _5ff=this.createRange(_5fd,len);
if(_5ff.index<0){
return 0;
}
var o={};
var ret=0;
var _602=Number.MIN_VALUE;
for(var i=_5ff.index;i>=_5ff.start;i--){
if(!o[_5fd[i].y]){
o[_5fd[i].y]=1;
}else{
o[_5fd[i].y]++;
}
}
for(var p in o){
if(_602<o[p]){
_602=o[p];
ret=p;
}
}
return ret;
}}});
dojo.provide("dojo.charting.Plot");
dojo.charting.RenderPlotSeries={Singly:"single",Grouped:"grouped"};
dojo.charting.Plot=function(_605,_606,_607){
var id="dojo-charting-plot-"+dojo.charting.Plot.count++;
this.getId=function(){
return id;
};
this.setId=function(key){
id=key;
};
this.axisX=null;
this.axisY=null;
this.series=[];
this.dataNode=null;
this.renderType=dojo.charting.RenderPlotSeries.Singly;
if(_605){
this.setAxis(_605,"x");
}
if(_606){
this.setAxis(_606,"y");
}
if(_607){
for(var i=0;i<_607.length;i++){
this.addSeries(_607[i]);
}
}
};
dojo.charting.Plot.count=0;
dojo.extend(dojo.charting.Plot,{addSeries:function(_60b,_60c){
if(_60b.plotter){
this.series.push(_60b);
}else{
this.series.push({data:_60b,plotter:_60c||dojo.charting.Plotters["Default"]});
}
},setAxis:function(axis,_60e){
if(_60e.toLowerCase()=="x"){
this.axisX=axis;
}else{
if(_60e.toLowerCase()=="y"){
this.axisY=axis;
}
}
},getRanges:function(){
var xmin,xmax,ymin,ymax;
xmin=ymin=Number.MAX_VALUE;
xmax=ymax=Number.MIN_VALUE;
for(var i=0;i<this.series.length;i++){
var _614=this.series[i].data.evaluate();
for(var j=0;j<_614.length;j++){
var comp=_614[j];
xmin=Math.min(comp.x,xmin);
ymin=Math.min(comp.y,ymin);
xmax=Math.max(comp.x,xmax);
ymax=Math.max(comp.y,ymax);
}
}
return {x:{upper:xmax,lower:xmin},y:{upper:ymax,lower:ymin},toString:function(){
return "[ x:"+xmax+" - "+xmin+", y:"+ymax+" - "+ymin+"]";
}};
},destroy:function(){
var node=this.dataNode;
while(node&&node.childNodes&&node.childNodes.length>0){
node.removeChild(node.childNodes[0]);
}
this.dataNode=null;
}});
dojo.provide("dojo.charting.PlotArea");
dojo.charting.PlotArea=function(){
var id="dojo-charting-plotarea-"+dojo.charting.PlotArea.count++;
this.getId=function(){
return id;
};
this.setId=function(key){
id=key;
};
this.areaType="standard";
this.plots=[];
this.size={width:600,height:400};
this.padding={top:10,right:10,bottom:20,left:20};
this.nodes={main:null,area:null,background:null,axes:null,plots:null};
this._color={h:140,s:120,l:120,step:27};
};
dojo.charting.PlotArea.count=0;
dojo.extend(dojo.charting.PlotArea,{nextColor:function(){
var rgb=dojo.gfx.color.hsl2rgb(this._color.h,this._color.s,this._color.l);
this._color.h=(this._color.h+this._color.step)%360;
while(this._color.h<140){
this._color.h+=this._color.step;
}
return dojo.gfx.color.rgb2hex(rgb[0],rgb[1],rgb[2]);
},getArea:function(){
return {left:this.padding.left,right:this.size.width-this.padding.right,top:this.padding.top,bottom:this.size.height-this.padding.bottom,toString:function(){
var a=[this.top,this.right,this.bottom,this.left];
return "["+a.join()+"]";
}};
},getAxes:function(){
var axes={};
for(var i=0;i<this.plots.length;i++){
var plot=this.plots[i];
axes[plot.axisX.getId()]={axis:plot.axisX,drawAgainst:plot.axisY,plot:plot,plane:"x"};
axes[plot.axisY.getId()]={axis:plot.axisY,drawAgainst:plot.axisX,plot:plot,plane:"y"};
}
return axes;
},getLegendInfo:function(){
var a=[];
for(var i=0;i<this.plots.length;i++){
for(var j=0;j<this.plots[i].series.length;j++){
var data=this.plots[i].series[j].data;
a.push({label:data.label,color:data.color});
}
}
return a;
},setAxesRanges:function(){
var _623={};
var axes={};
for(var i=0;i<this.plots.length;i++){
var plot=this.plots[i];
var _623=plot.getRanges();
var x=_623.x;
var y=_623.y;
var ax,ay;
if(!axes[plot.axisX.getId()]){
axes[plot.axisX.getId()]=plot.axisX;
_623[plot.axisX.getId()]={upper:x.upper,lower:x.lower};
}
ax=_623[plot.axisX.getId()];
ax.upper=Math.max(ax.upper,x.upper);
ax.lower=Math.min(ax.lower,x.lower);
if(!axes[plot.axisY.getId()]){
axes[plot.axisY.getId()]=plot.axisY;
_623[plot.axisY.getId()]={upper:y.upper,lower:y.lower};
}
ay=_623[plot.axisY.getId()];
ay.upper=Math.max(ay.upper,y.upper);
ay.lower=Math.min(ay.lower,y.lower);
}
for(var p in axes){
axes[p].range=_623[p];
}
},render:function(_62c,_62d){
if(!this.nodes.main||!this.nodes.area||!this.nodes.background||!this.nodes.plots||!this.nodes.axes){
this.initialize();
}
this.resize();
for(var i=0;i<this.plots.length;i++){
var plot=this.plots[i];
if(plot.dataNode){
this.nodes.plots.removeChild(plot.dataNode);
}
var _630=this.initializePlot(plot);
switch(plot.renderType){
case dojo.charting.RenderPlotSeries.Grouped:
if(plot.series[0]){
_630.appendChild(plot.series[0].plotter(this,plot,_62c,_62d));
}
break;
case dojo.charting.RenderPlotSeries.Singly:
default:
for(var j=0;j<plot.series.length;j++){
var _632=plot.series[j];
var data=_632.data.evaluate(_62c);
_630.appendChild(_632.plotter(data,this,plot,_62d));
}
}
this.nodes.plots.appendChild(_630);
}
},destroy:function(){
for(var i=0;i<this.plots.length;i++){
this.plots[i].destroy();
}
for(var p in this.nodes){
var node=this.nodes[p];
if(!node){
continue;
}
if(!node.childNodes){
continue;
}
while(node.childNodes.length>0){
node.removeChild(node.childNodes[0]);
}
this.nodes[p]=null;
}
}});
dojo.provide("dojo.charting.svg.PlotArea");
if(dojo.render.svg.capable){
dojo.extend(dojo.charting.PlotArea,{resize:function(){
var area=this.getArea();
this.nodes.area.setAttribute("width",this.size.width);
this.nodes.area.setAttribute("height",this.size.height);
var rect=this.nodes.area.getElementsByTagName("rect")[0];
rect.setAttribute("x",area.left);
rect.setAttribute("y",area.top);
rect.setAttribute("width",area.right-area.left);
rect.setAttribute("height",area.bottom-area.top);
this.nodes.background.setAttribute("width",this.size.width);
this.nodes.background.setAttribute("height",this.size.height);
if(this.nodes.plots){
this.nodes.area.removeChild(this.nodes.plots);
this.nodes.plots=null;
}
this.nodes.plots=document.createElementNS(dojo.svg.xmlns.svg,"g");
this.nodes.plots.setAttribute("id",this.getId()+"-plots");
this.nodes.plots.setAttribute("style","clip-path:url(#"+this.getId()+"-clip);");
this.nodes.area.appendChild(this.nodes.plots);
for(var i=0;i<this.plots.length;i++){
this.nodes.plots.appendChild(this.initializePlot(this.plots[i]));
}
if(this.nodes.axes){
this.nodes.area.removeChild(this.nodes.axes);
this.nodes.axes=null;
}
this.nodes.axes=document.createElementNS(dojo.svg.xmlns.svg,"g");
this.nodes.axes.setAttribute("id",this.getId()+"-axes");
this.nodes.area.appendChild(this.nodes.axes);
var axes=this.getAxes();
for(var p in axes){
var obj=axes[p];
this.nodes.axes.appendChild(obj.axis.initialize(this,obj.plot,obj.drawAgainst,obj.plane));
}
},initializePlot:function(plot){
plot.destroy();
plot.dataNode=document.createElementNS(dojo.svg.xmlns.svg,"g");
plot.dataNode.setAttribute("id",plot.getId());
return plot.dataNode;
},initialize:function(){
this.destroy();
this.nodes.main=document.createElement("div");
this.nodes.area=document.createElementNS(dojo.svg.xmlns.svg,"svg");
this.nodes.area.setAttribute("id",this.getId());
this.nodes.main.appendChild(this.nodes.area);
var defs=document.createElementNS(dojo.svg.xmlns.svg,"defs");
var clip=document.createElementNS(dojo.svg.xmlns.svg,"clipPath");
clip.setAttribute("id",this.getId()+"-clip");
var rect=document.createElementNS(dojo.svg.xmlns.svg,"rect");
clip.appendChild(rect);
defs.appendChild(clip);
this.nodes.area.appendChild(defs);
this.nodes.background=document.createElementNS(dojo.svg.xmlns.svg,"rect");
this.nodes.background.setAttribute("id",this.getId()+"-background");
this.nodes.background.setAttribute("fill","#fff");
this.nodes.area.appendChild(this.nodes.background);
this.resize();
return this.nodes.main;
}});
}
dojo.provide("dojo.charting.vml.PlotArea");
if(dojo.render.vml.capable){
dojo.extend(dojo.charting.PlotArea,{resize:function(){
var a=this.getArea();
this.nodes.area.style.width=this.size.width+"px";
this.nodes.area.style.height=this.size.height+"px";
this.nodes.background.style.width=this.size.width+"px";
this.nodes.background.style.height=this.size.height+"px";
this.nodes.plots.width=this.size.width+"px";
this.nodes.plots.height=this.size.height+"px";
this.nodes.plots.style.clip="rect("+a.top+" "+a.right+" "+a.bottom+" "+a.left+")";
if(this.nodes.axes){
this.nodes.area.removeChild(this.nodes.axes);
}
var axes=this.nodes.axes=document.createElement("div");
axes.id=this.getId()+"-axes";
this.nodes.area.appendChild(axes);
var ax=this.getAxes();
for(var p in ax){
var obj=ax[p];
axes.appendChild(obj.axis.initialize(this,obj.plot,obj.drawAgainst,obj.plane));
}
},initializePlot:function(plot){
plot.destroy();
plot.dataNode=document.createElement("div");
plot.dataNode.id=plot.getId();
return plot.dataNode;
},initialize:function(){
this.destroy();
var main=this.nodes.main=document.createElement("div");
var area=this.nodes.area=document.createElement("div");
area.id=this.getId();
area.style.position="absolute";
main.appendChild(area);
var bg=this.nodes.background=document.createElement("div");
bg.id=this.getId()+"-background";
bg.style.position="absolute";
bg.style.top="0px";
bg.style.left="0px";
bg.style.backgroundColor="#fff";
area.appendChild(bg);
var a=this.getArea();
var _64b=this.nodes.plots=document.createElement("div");
_64b.id=this.getId()+"-plots";
_64b.style.position="absolute";
_64b.style.top="0px";
_64b.style.left="0px";
area.appendChild(_64b);
for(var i=0;i<this.plots.length;i++){
_64b.appendChild(this.initializePlot(this.plots[i]));
}
this.resize();
return main;
}});
}
dojo.provide("dojo.charting.Chart");
dojo.charting.Chart=function(node,_64e,_64f){
this.node=node||null;
this.title=_64e||"Chart";
this.description=_64f||"";
this.plotAreas=[];
};
dojo.extend(dojo.charting.Chart,{addPlotArea:function(obj,_651){
if(obj.x!=null&&obj.left==null){
obj.left=obj.x;
}
if(obj.y!=null&&obj.top==null){
obj.top=obj.y;
}
this.plotAreas.push(obj);
if(_651){
this.render();
}
},onInitialize:function(_652){
},onRender:function(_653){
},onDestroy:function(_654){
},initialize:function(){
if(!this.node){
dojo.raise("dojo.charting.Chart.initialize: there must be a root node defined for the Chart.");
}
this.destroy();
this.render();
this.onInitialize(this);
},render:function(){
if(this.node.style.position!="absolute"){
this.node.style.position="relative";
}
for(var i=0;i<this.plotAreas.length;i++){
var area=this.plotAreas[i].plotArea;
var node=area.initialize();
node.style.position="absolute";
node.style.top=this.plotAreas[i].top+"px";
node.style.left=this.plotAreas[i].left+"px";
this.node.appendChild(node);
area.render();
}
},destroy:function(){
for(var i=0;i<this.plotAreas.length;i++){
this.plotAreas[i].plotArea.destroy();
}
while(this.node&&this.node.childNodes&&this.node.childNodes.length>0){
this.node.removeChild(this.node.childNodes[0]);
}
}});

