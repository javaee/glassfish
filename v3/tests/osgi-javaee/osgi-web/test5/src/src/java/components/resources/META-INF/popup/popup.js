
var timerObject;
var req=initRequest();
var isIE;
var doOnce=false;
        
    function loadOnce() {
        if(navigator.appName == "Microsoft Internet Explorer") {
            // set images for ie so it will be transparent
            document.getElementById("topcapimg").src="images/ie_transparency.png";
            document.getElementById("bottomcapimg").src="images/ie_transparency.png";
            document.getElementById("arrowleftdown").src="images/ie_transparency.png";
            document.getElementById("arrowleftup").src="images/ie_transparency.png";
            document.getElementById("arrowrightdown").src="images/ie_transparency.png";
            document.getElementById("arrowrightup").src="images/ie_transparency.png";
        }
    }        
        
    function initRequest() {
      if (window.XMLHttpRequest) {
        return new XMLHttpRequest();
      } else if (window.ActiveXObject) {
        isIE = true;
        return new ActiveXObject("Microsoft.XMLHTTP");
      }
    }


    function hide(jsfCompName) {
        // hide popup
        document.getElementById("popup" + jsfCompName).style.visibility='hidden';
        // clear timeout in the case user is just moving mouse over page
        clearTimeout(timerObject);
    }

    
    // still need to pass specific servlet url and parseMessage function to generic functions    
    function show(jsfCompName, eventx, paramx) {
    
        if(!doOnce) {
            loadOnce();
            doOnce=true;
        }

        pMainFunction=eval("mainFunction" + jsfCompName);

        // need to send in parse function and url ???
        ajax=new pMainFunction(jsfCompName, eventx, paramx);

        // set timeout to 1 second so popup is flashing when user is moving mouse over page
        timerObject=setTimeout("showInternal(ajax)", 1000);
    }    

    
    function showInternal(ajax) {
        // calculate arrow and border image location
        req=initRequest();
        var url = ajax.urlx + escape(ajax.targetParam);
        req.onreadystatechange = ajax.processRequestFunction;
        req.open("GET", url, true);
        req.send(null);
        
        setImageLocation(ajax);
        popupId="popup" + ajax.componentId;
        document.getElementById(popupId).style.visibility='visible';
    }

    function setImageLocation(ajax) {
        // find which quadrant
        // get screen size for quadrant calc
        var sxx=getWindowWidth()/2;
        var syy=getWindowHeight()/2;
        var xx=0;
        var yy=0;
        var axx=0;
        var ayy=0;
        var quadrant="";


        popupArrowId="popupArrow" + ajax.componentId;
        popupArrowLD="arrowleftdownid" + ajax.componentId;
        popupArrowLU="arrowleftupid" + ajax.componentId;
        popupArrowRD="arrowrightdownid" + ajax.componentId;
        popupArrowRU="arrowrightupid" + ajax.componentId;
        popupId="popup" + ajax.componentId;


        if(ajax.clientTargetX < sxx && ajax.clientTargetY < syy) {
            // quadrant 1
            quadrant="quadrant 1";
            document.getElementById(popupArrowLD).style.display="inline";
            document.getElementById(popupArrowLU).style.display="none";
            document.getElementById(popupArrowRD).style.display="none";
            document.getElementById(popupArrowRU).style.display="none";
    
            // position the popup
            xx=ajax.targetX + 75;
            yy=ajax.targetY - 50;
            /*
            if(yy < 0) {
                yy=0;
            }
            */

            if(ajax.clientTargetY < 50 && ajax.clientTargetY != ajax.targetY) {
                // need to account for page scolling with the link right at the top
                ayy=5;
            } else if(yy > 0) {
                ayy=45;
            }

            
            document.getElementById(popupId).style.left=xx + "px";
            document.getElementById(popupId).style.top=yy + "px";
            
            // position arrow within the popup
            axx=-73;
            document.getElementById(popupArrowId).style.left=axx + "px";            
            ayy=ajax.targetY;
            if(yy > 0) {
                ayy=45;
            }
            
            document.getElementById(popupArrowId).style.top=ayy + "px";
            
        } else if(ajax.clientTargetX < sxx && ajax.clientTargetY > syy) { 
            // quadrant 2
            quadrant="quadrant 2";
            
            document.getElementById(popupArrowLD).style.display="none";
            document.getElementById(popupArrowLU).style.display="inline";
            document.getElementById(popupArrowRD).style.display="none";
            document.getElementById(popupArrowRU).style.display="none";
            
            // position the popup
            xx=ajax.targetX + 75;
            yy=ajax.targetY - 190;
            if(yy < 0) {
                yy=0;
            }
            document.getElementById(popupId).style.left=xx + "px";
            document.getElementById(popupId).style.top=yy + "px";
            
            // position arrow within the popup
            axx=-73;
            document.getElementById(popupArrowId).style.left=axx + "px";
            if(yy > 0) {
                ayy=40;
            } else {
                // pull pack to try and match cursor
                ayy=ajax.targetY - 150;
                if(ayy < 5) ayy=5;
            }
            document.getElementById(popupArrowId).style.top=ayy + "px";
            
        } else if(ajax.clientTargetX > sxx && ajax.clientTargetY < syy) { 
            // quadrant 3
            quadrant="quadrant 3";
            
            document.getElementById(popupArrowLD).style.display="none";
            document.getElementById(popupArrowLU).style.display="none";
            document.getElementById(popupArrowRD).style.display="inline";
            document.getElementById(popupArrowRU).style.display="none";
            
            // position the popup
            xx=ajax.targetX - 375;
            yy=ajax.targetY - 50;
            if(yy < 0) {
                yy=0;
            }
            document.getElementById(popupId).style.left=xx + "px";
            document.getElementById(popupId).style.top=yy + "px";
            
            // position arrow within the popup
            axx=291;
            document.getElementById(popupArrowId).style.left=axx + "px";
            ayy=ajax.targetY;
            /*
            if(yy > 0) {
                ayy=45;
            }
            */
            if(ajax.clientTargetY < 50 && ajax.clientTargetY != ajax.targetY) {
                // need to account for page scolling with the link right at the top
                yy=ajax.targetY;
            } else if(yy < 0) {
                yy=0;
            }
            
            document.getElementById(popupArrowId).style.top=ayy + "px";
            
        } else if(ajax.clientTargetX > sxx && ajax.clientTargetY > syy) { 
            // quadrant 4
            quadrant="quadrant 4";

            document.getElementById(popupArrowLD).style.display="none";
            document.getElementById(popupArrowLU).style.display="none";
            document.getElementById(popupArrowRD).style.display="none";
            document.getElementById(popupArrowRU).style.display="inline";
            
            // position the popup
            xx=ajax.targetX - 375;
            yy=ajax.targetY - 190;
            if(yy < 0) {
                yy=0;
            }
            document.getElementById(popupId).style.left=xx + "px";
            document.getElementById(popupId).style.top=yy + "px";
            
            // position arrow within the popup
            axx=291;
            document.getElementById(popupArrowId).style.left=axx + "px";
            if(yy > 0) {
                ayy=40;
            } else {
                // pull pack to try and match cursor
                ayy=ajax.targetY - 150;
                if(ayy < 5) ayy=5;
            }
            document.getElementById(popupArrowId).style.top=ayy + "px";
        }
    
        //document.getElementById("test").firstChild.nodeValue=quadrant + " - " + sxx + "," + syy;
        //document.getElementById("test1").firstChild.nodeValue="actual box coords x,y=" + ajax.targetX + "," + ajax.targetY + 
        //  " - bx,by=" +  xx + "," + yy + 
        //  " - ax,ay=" + axx + "," + ayy;

    }
    

function getWindowHeight() {
    var height=0;
	if(typeof(window.innerWidth) == 'number' ) {
	    height=window.innerHeight;
	} else if(document.documentElement && document.documentElement.clientHeight) {
	    height=document.documentElement.clientHeight;
	} else if(document.body && document.body.clientHeight) {
	    height=document.body.clientHeight;
	}
    return height;
}    

function getWindowWidth() {
    var width=0;
	if(typeof(window.innerWidth ) == 'number') {
	    width=window.innerWidth;
	} else if(document.documentElement && document.documentElement.clientWidth) {
	    width=document.documentElement.clientWidth;
	} else if(document.body && document.body.clientWidth) {
	    width=document.body.clientWidth;
	}
    return width;
}    

