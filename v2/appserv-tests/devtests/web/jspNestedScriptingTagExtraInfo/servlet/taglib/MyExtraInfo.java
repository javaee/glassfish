package taglib;

import javax.servlet.jsp.tagext.*;

public class MyExtraInfo extends TagExtraInfo {

    public VariableInfo[] getVariableInfo(TagData inData) {
        return new VariableInfo[] {
            new VariableInfo(inData.getAttributeString("name"),
                "java.lang.Integer", true, VariableInfo.AT_BEGIN)};
    }
}
