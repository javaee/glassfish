<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

${pageContext.request.reader}

<c:set target="${pageContext.response}"
       property="characterEncoding"
       value="${pageContext.request.characterEncoding}" />
