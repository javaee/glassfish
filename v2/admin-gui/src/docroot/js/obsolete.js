
/*
 * The following codes seems to be obsoleted.  No one is using it.
 */

var enableDisableElement = [];  
var enableClassName = [];  
var disableClassName = [];  

function createCheckBoxName (checkBox,idNum) {
    var separator = ":";
    var newSeparator = "";
    var name = "";
    var checkboxName = checkBox.name;
    arrayOfStrings = checkboxName.split(separator);
    for (i=0; i<arrayOfStrings.length; i++) {
        name = name + newSeparator;
        value = arrayOfStrings[i]; 
        if (i == 3) {
            value = idNum
        }
        name = name + value;
        newSeparator = ":";
    }
    return name;
}

function clearEnableDisableElements ()
{
    enableDisableElement = [];
    enableClassName = [];
    disableClassName = [];
}


function addEnableDisableElement (elementName, enableClass, disableClass)
{
    var element = document.getElementById (elementName);
    if (element != null) {
        enableDisableElement.push(element); 
        enableClassName.push(enableClass);
        disableClassName.push(disableClass);
    }
}


function addEnableDisableButton (name) {
   // addEnableDisableElement (name,'Btn1','Btn1Dis');
    addEnableDisableElement (name,'Btn1_sun4','Btn1Dis_sun4');
}


function addEnableDisableDropdown (name) {
    addEnableDisableElement (name,'MnuJmp','MnuJmp');
}


function enableDisableElements(checkBox) {
    var numberOfElements = enableDisableElement.length;
    for (var count=0; count<numberOfElements; count++) {
        var element = enableDisableElement.pop();
        var enableClass = enableClassName.pop();
        var disableClass = disableClassName.pop();
        var done = false;
        var checked = false;
        var checkBoxName = "";
        for (var i=0; done==false; i++) {
            checkBoxName = createCheckBoxName (checkBox,i);
            checkBoxElement = document.getElementById (checkBoxName);
            if (checkBoxElement != null) {
                if (checkBoxElement.checked) {
                    checked = true;
                }
            } else {
                done = true;
            }
        }   
        if (checked) {
            element.disabled = false;
            element.className = enableClass;
        } else {
            element.disabled = true;
            element.className = disableClass;
        }
    }
//    alert ($pageSession{tableId});
//    var table = document.getElementById("$pageSession{tableId}");
    var table = document.getElementById("sharedTableEEForm:sharedTableEE");
    table.initAllRows();
}


function enableElements ()
{
    var numberOfElements = enableDisableElement.length;
    for (var count=0; count<numberOfElements; count++) {
        var element = enableDisableElement.pop();
        var enableClass = enableClassName.pop();
        element.disabled = false;
        element.className = enableClass;
    }
}


function disableElements ()
{
    var numberOfElements = enableDisableElement.length;
    for (var count=0; count<numberOfElements; count++) {
        var element = enableDisableElement.pop();
        var disableClass = disableClassName.pop();
        element.disabled = true;
        element.className = disableClass;
    }
}

//============================================
