<%!
  public interface UserInformation {
      Integer getUserID();
  }
%>

<%
  test.Document doc = new test.Document(new UserInformation() {
      public Integer getUserID() {
          return null;
      }
  });
%>