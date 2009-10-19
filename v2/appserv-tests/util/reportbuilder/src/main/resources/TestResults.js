function showHide(id) {
    var element = document.getElementById(id);
    if (element.style.display == 'block') {
        element.style.display = 'none';
    } else {
        element.style.display = 'block';
    }
}
var statii = new Array();
statii[0] = "pass";
statii[1] = "fail";
statii[2] = "didnotrun";
function toggleResults() {
    var allHTMLTags = document.getElementsByTagName('tr');
    for (var i in allHTMLTags) {
        var show = false;
        var styled = false;
        for (var index in statii) {
            var theClass = statii[index]
            var input = document.getElementById('summary').getElementsByTagName('input')[index];
            if (allHTMLTags[i].className.indexOf(theClass) != -1) {
                show = show || input.checked
                styled = true
            }
        }
        if (styled) {
            if (show) {
                allHTMLTags[i].style.display = 'table-row';
            } else {
                allHTMLTags[i].style.display = 'none';
            }
        }
    }
}
