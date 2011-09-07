var lazyLoadElements = [];

function doLazyLoad() {
    function isScrolledIntoView(elem) {
        var docViewTop = $(window).scrollTop(),
        docViewBottom = docViewTop + $(window).height(),
        elemTop = $(elem).offset().top,
        elemBottom = elemTop + $(elem).height();
        //Is more than half of the element visible
        return ((elemTop + ((elemBottom - elemTop)/2)) >= docViewTop && ((elemTop + ((elemBottom - elemTop)/2)) <= docViewBottom));
    }

    for (var i = 0; i < lazyLoadElements.length; i++) {
        var el = document.getElementById(lazyLoadElements[i]);
        if (el) {
            if (isScrolledIntoView(el)) {
                lazyLoadElements.splice(i,1);
                console.debug(el.id + " is now visible!");
                jsf.ajax.request(el.id, null, {render:'@form'});
            }
        }
    }
}

$(document).scroll(doLazyLoad);
$(document).ready(doLazyLoad);
