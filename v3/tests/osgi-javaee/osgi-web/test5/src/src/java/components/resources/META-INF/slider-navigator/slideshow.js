/* Copyright 2005 Sun Microsystems, Inc. All rights reserved.
   You may not modify, use, reproduce, or distribute this software except in
   compliance with the terms of the License at:
   http://developer.sun.com/berkeley_license.html
$Id: slideshow.js,v 1.1 2005/11/19 00:54:43 inder Exp $
*/

// The absolute position left(px)
//var layerLeft = 100;
// The absolute position top(px)
//var layerTop = 100;

// The absolute width(px) of the slideshow frame
// The gap between this value and the actual image size is
// the gap between the images.
//var layerWidth = 130;

// The absolute height of the frame.
//var layerHeight = 90;

var scrollerbgcolor='#efefef';
// Pause time when the image reaches the left side
//var pause = 3000;
// Moving speed. 60 looks appropriate.
//var speed = 60;
// Carries the Nth of the array to point to the next image.
// Right now, the array must have more than two images.
var nextImg = 2;
// Layer moving rate
var layerMoveRate = 6;

//var imgArray=new Array();
//imgArray[0]='<a href="http://java.sun.com"><img src="images/cat1.gif" border="0" title="neko1"></a>';
//imgArray[1]='<a href="http://java.sun.com"><img src="images/cat2.gif" border="0" title="neko2"></a>';
//imgArray[2]='<a href="http://java.sun.com"><img src="images/cat3.gif" border="0" title="neko3"></a>';
//imgArray[3]='<a href="http://java.sun.com"><img src="images/cat4.gif" border="0" title="neko4"></a>';

function slide(layerlocation){
  layerObj=eval(layerlocation)
  currentXpos = parseInt(layerObj.style.left);

  if ((currentXpos > 0) && (currentXpos <= layerMoveRate)) {
    // if the layer is close to the left, force it to the left and
    // start process 1) continue to go to the left, 2) start the next layer.
    // Note that the layer is moving per layerMoveRate.
    layerObj.style.left = 0;
    setTimeout("slide(layerObj)", pause);
    setTimeout("slideOther(l2)", pause);
    return;
  }
  if (currentXpos >= layerObj.offsetWidth*-1){
    // Move the layer per layerMoveRate from right to left.
    // untill the right side of the image is move beyond the layer offset.
    layerObj.style.left = parseInt(layerObj.style.left)-layerMoveRate;
    setTimeout("slide(layerObj)", speed);

  } else{
    // Set this layer's x position to the right side and fetch the next image.
    layerObj.style.left=layerWidth;
    layerObj.innerHTML=imgArray[nextImg];
    if (nextImg==imgArray.length-1)
      nextImg=0;
    else
      nextImg++;
  }
}

// This function is exactly the same as slide(), except the
// calling sequence of the slide*() functions recursively.
// Need to call these two functions in a toggle way.
function slideOther(layerLocation){
  otherLayer = eval(layerLocation);
  xpos2 = parseInt(otherLayer.style.left);

  if ((xpos2 > 0) && (xpos2 <= layerMoveRate)){
    otherLayer.style.left = 0;
    setTimeout("slideOther(otherLayer)",pause);
    setTimeout("slide(l1)",pause);
    return;
  }
  if (xpos2 >= layerObj.offsetWidth*-1){
    otherLayer.style.left = parseInt(otherLayer.style.left)-layerMoveRate;
    setTimeout("slideOther(l2)", speed);

  } else{
    otherLayer.style.left=layerWidth;
    otherLayer.innerHTML=imgArray[nextImg];
    if (nextImg==imgArray.length-1)
      nextImg=0;
    else
      nextImg++;
  }
}

function startscroll(){
  
  if (document.all) {
    l1 = layerOne;
    l2 = layerTwo;
  } else if (document.getElementById) {
    l1 = document.getElementById("layerOne");
    l2 = document.getElementById("layerTwo");
  }
  l1.innerHTML=imgArray[0];
  l2.innerHTML=imgArray[1];
  slide(l1, 0);
  // set next layer's x position to the "ready" position.
  l2.style.left = layerWidth;
}

function viewLarge(filepath) {
  var viewObj = document.getElementById("limg");
  viewObj.src = filepath;
}

function popupImage(path){
  var pObj = new Image();
  pObj.src = path;
  window.open(path,"pw","menubar=no,scrollbars=no,resizable=yes,width="+(pObj.width+20)+",height="+(pObj.height+30));
}
/*********
  // Base layer which is also used to hide the following layer.
  document.write('<div style="position:absolute; left:' + layerLeft + 
                 '; top:' + layerTop + '; width:' + layerWidth + 
                 '; height:' + layerHeight + 
                 '; overflow:hidden; background-color:' + scrollerbgcolor + '">')
  // 1st layer
  document.write('<div id="layerOne" style="position:absolute;width:' +
                  layerWidth + ';left:1px;top:0px;">')
  document.write('</div>')
  // 2nd layer
  document.write('<div id="layerTwo" style="position:absolute;width:'+layerWidth+';left:0px;top:0px">')
  document.write('</div>')
  document.write('</div>')
  window.onLoad = startscroll();
*********/


