package taglib;

import javax.servlet.jsp.tagext.*;

public class MyTagExtraInfo extends TagExtraInfo {

    public ValidationMessage[] validate(TagData data) {

        ValidationMessage[] vms = null;

        TagLibraryInfo[] infos =
            getTagInfo().getTagLibrary().getTagLibraryInfos();
        if (infos.length != 1) {
            vms = new ValidationMessage[1];
            vms[0] = new ValidationMessage(null, "Wrong number of tsglibs");
        }

	return vms;
    }
}

