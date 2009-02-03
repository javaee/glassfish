<f:verbatim>
<div style="height: 435px;background-image: url(#{request.contextPath}/resource/customTheme/images/login-backimage-open.png);background-repeat:no-repeat;background-position:left top; width: 720px; margin: auto;">
    <div style="width: 460px; padding-top: 160px; margin-left: 260px;">
</f:verbatim>
        <sun:image url="#{request.contextPath}/resource/customTheme/images/login-product_name_open.png" alt="Glassfish V3 Prelude" height="21" width="198" />
<f:verbatim>
        <form method="POST" class="form" name="loginform" action="j_security_check">
        <table>
        <tr>
            <td><label for="Login.username" style="font-weight: bold;">$resource{i18n.common.username}</label></td>
            <td><input type="text" name="j_username" id="Login.username" tabindex="1" value="#{anonLogin ? 'anonymous' : ''}"></td>
        </tr>
        <tr>
            <td><label for="Login.password" style="font-weight: bold;">$resource{i18n.common.password}</label>
            <td><input type="password" name="j_password" id="Login.password" onKeyPress="return submitenter(event, 'loginButton', '$resource{i18n.loginPageTitle}')" tabindex="2">
        <tr>
            <td colspan="2" align="center">
                <input type="submit" class="Btn1"
                    value="$resource{i18n.loginPageTitle}"
                    title="#{loginButtonTooltip}" tabindex="3"
                    onmouseover="javascript: if (this.disabled==0) this.className='Btn1Hov'"
                    onmouseout="javascript: if (this.disabled==0) this.className='Btn1'"
                    onblur="javascript: if (this.disabled==0) this.className='Btn1'"
                    onfocus="javascript: if (this.disabled==0) this.className='Btn1Hov'"
                    onclick="javascript: submitAndDisable(this, '$resource{i18n.loginPageTitle}');"
                    onkeypress="javascript: submitenter(event, 'loginButton', '$resource{i18n.loginPageTitle}');"
                    name="loginButton" id="loginButton">
        	    <input type="hidden" name="loginButton.DisabledHiddenField" value="true" />
    	    </td>
	    </tr>
	    </table>
        </form>
    </div>
</div>
</f:verbatim>
