package appmgttest;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EnvEntryInfo {

    /** env pattern is name(class)=value//desc */
    private static final String BRIEF_PATTERN_STRING = "([^\\(]*)\\(([^\\)]*)\\)=(.*?)";
    private static final String FULL_PATTERN_STRING = BRIEF_PATTERN_STRING + "//(.*)";

    private static final Pattern BRIEF_PATTERN = Pattern.compile(BRIEF_PATTERN_STRING);
    private static final Pattern FULL_PATTERN = Pattern.compile(FULL_PATTERN_STRING);

    private final String name;
    private final String className;
    private final String value;
    private final String desc;

    public EnvEntryInfo(final String name, final String cl, final String value, final String desc) {
        super();
        this.name = name;
        this.className = cl;
        this.value = value;
        this.desc = desc;
    }

    public EnvEntryInfo(final String name, final String cl, final String value) {
        this(name, cl, value, null);
    }

    public static EnvEntryInfo parseFull(final String expr) throws ClassNotFoundException {
        return parse(expr, FULL_PATTERN);
    }

    public static EnvEntryInfo parseBrief(final String expr) throws ClassNotFoundException {
        return parse(expr, BRIEF_PATTERN);
    }



    private static EnvEntryInfo parse(final String expr, final Pattern p) throws ClassNotFoundException {
        Matcher m = p.matcher(expr);
        EnvEntryInfo result = null;
        if (m.matches()) {
            result = new EnvEntryInfo(m.group(1), m.group(2),
                    m.group(3), m.groupCount() > 3 ? m.group(4) : null);
        }
        return result;
    }

    public String name() {
        return name;
    }

    @Override
    public String toString() {
        return String.format("%1$s(%2$s)=%3$s//\"%4$s\"", name, className, value, desc);
    }

    public String toStringBrief() {
        return String.format("%1$s(%2$s)=%3$s", name, className, value);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final EnvEntryInfo other = (EnvEntryInfo) obj;
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        if (this.className != other.className && (this.className == null || !this.className.equals(other.className))) {
            return false;
        }
        if ((this.value == null) ? (other.value != null) : !this.value.equals(other.value)) {
            return false;
        }
        if ((this.desc == null) ? (other.desc != null) : !this.desc.equals(other.desc)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 67 * hash + (this.className != null ? this.className.hashCode() : 0);
        hash = 67 * hash + (this.value != null ? this.value.hashCode() : 0);
        hash = 67 * hash + (this.desc != null ? this.desc.hashCode() : 0);
        return hash;
    }



}
