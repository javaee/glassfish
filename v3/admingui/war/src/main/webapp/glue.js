jmaki.namespace("jmaki.listeners");

// programatcially add a glue listener    
jmaki.subscribe( "/jmaki/showURL", "jmaki.listeners.urlHandler");

// this is the listener for /jmaki/showURL
// the args is a string in this case that is a link to a blog entry
jmaki.listeners.urlHandler = function(args) {
  // publish to the topic the RSS display is mapped to
  jmaki.publish("/jmaki/rssContents/setInclude", { value : args});
}


jmaki.debug=false;
jmaki.debugGlue=false;
jmaki.namespace("jmaki.filters");

jmaki.namespace("admin.blockList.RSSFilter");

admin.blockList.RSSFilter = function(input) {
    return input.channel.items;
};

admin.blockList.RemoveFirstImageFilter = function(input) {
    // Remove all but the first image
    var len = input.channel.items.length;
    for (var cnt = 1; cnt < len; cnt++) {
        var desc = input.channel.items[cnt].description;
        var idx = desc.indexOf('<img');
        if (idx != -1) {
            var idx2 = desc.indexOf('>', idx);
            if (idx2 != -1) {
                var newDesc = desc.substring(0, idx);
                newDesc += desc.substring(idx2 + 1);
                input.channel.items[cnt].description = newDesc;
            }
        }
    }
    return input.channel.items;
};

