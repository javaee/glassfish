
dojo.require("dojo.io.cometd");

function $() {
  return document.getElementById(arguments[0]);
}


var EvUtil =
{
    getKeyCode : function(ev)
    {
        var keyc;
        if (window.event)
            keyc=window.event.keyCode;
        else
            keyc=ev.keyCode;
        return keyc;
    }
};

var room = 
{
  _last: "",
  _username: null,
  
  join: function(name)
  {
    if (name == null || name.length==0 )
    {
      alert('Please enter a username!');
    }
    else
    {
       this._username=name;
       $('join').className='hidden';
       $('joined').className='';
       $('phrase').focus();
       Behaviour.apply();
       
       
       // Really need to batch to avoid ordering issues
	   cometd.subscribe("/chat/demo", false, room, "_chat");
	   cometd.publish("/chat/demo", { user: room._username, join: true, chat : room._username+" has joined"});
	   
       // XXX ajax.sendMessage('join', room._username);
    }
  },
  
  leave: function()
  {
       cometd.unsubscribe("/chat/demo", false, room, "_chat");
       cometd.publish("/chat/demo", { user: room._username, leave: true, chat : room._username+" has left"});
	   
       // switch the input form
       $('join').className='';
       $('joined').className='hidden';
       $('username').focus();
       Behaviour.apply();
       // XXX ajax.sendMessage('leave',room._username);
       room._username=null;
  },
  
  chat: function(text)
  {
    if (text != null && text.length>0 )
    {
    	// lame attempt to prevent markup    
    	text=text.replace(/</g,'&lt;');
    	text=text.replace(/>/g,'&gt;');
    	
        // XXX ajax.sendMessage('chat',text);
        cometd.publish("/chat/demo", { user: room._username, chat: text});
    }
  },
  
  _chat: function(message)
  {
     var chat=$('chat');
     if (!message.data)
     {
        alert("bad message format "+message);
	return;
     }
     var from=message.data.user;
     var special=message.data.join || message.data.leave;
     var text=message.data.chat;
     if (text!=null)
     {
       if ( !special && from == room._last )
         from="...";
       else
       {
         room._last=from;
         from+=":";
       }
     
       if (special)
       {
         chat.innerHTML += "<span class=\"alert\"><span class=\"from\">"+from+"&nbsp;</span><span class=\"text\">"+text+"</span></span><br/>";
         room._last="";
       }
       else
         chat.innerHTML += "<span class=\"from\">"+from+"&nbsp;</span><span class=\"text\">"+text+"</span><br/>";
       chat.scrollTop = chat.scrollHeight - chat.clientHeight;    
     } 
  },
  
  _init: function()
  {
				
       // XXX ajax.addListener('chat',room._chat);
       // XXX ajax.addListener('joined',room._joined);
       // XXX ajax.addListener('left',room._left);
       // XXX ajax.addListener('members',room._members);
       $('join').className='';
       $('joined').className='hidden';
       $('username').focus();
      Behaviour.apply();
  }
};

Behaviour.addLoadEvent(room._init);  

var chatBehaviours = 
{ 
  '#username' : function(element)
  {
    element.setAttribute("autocomplete","OFF"); 
    element.onkeyup = function(ev)
    {          
        var keyc=EvUtil.getKeyCode(ev);
        if (keyc==13 || keyc==10)
        {
          room.join($('username').value);
	  return false;
	}
	return true;
    } 
  },
  
  '#joinB' : function(element)
  {
    element.onclick = function(event)
    {
      room.join($('username').value);
      return false;
    }
  },
  
  '#phrase' : function(element)
  {
    element.setAttribute("autocomplete","OFF");
    element.onkeyup = function(ev)
    {   
        var keyc=EvUtil.getKeyCode(ev);
        if (keyc==13 || keyc==10)
        {
          room.chat($('phrase').value);
          $('phrase').value='';
	  return false;
	}
	return true;
    }
  },
  
  '#sendB' : function(element)
  {
    element.onclick = function(event)
    {
      room.chat($('phrase').value);
      $('phrase').value='';
      return false;
    }
  },
  
  
  '#leaveB' : function(element)
  {
    element.onclick = function()
    {
      room.leave();
      return false;
    }
  }
};

Behaviour.register(chatBehaviours); 


