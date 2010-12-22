if (self == top) {
    var theBody = document.getElementsByTagName('body')[0];
    theBody.style.display = "block";
} else { 
    top.location = self.location; 
}