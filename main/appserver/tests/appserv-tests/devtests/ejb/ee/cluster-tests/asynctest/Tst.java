public class Tst {

public static void main(String[] args) {
   print("a:b:2:inst1");
   print("a::2:inst1");
   print(":b:2:inst1");
   print("::2:inst1");
   print(":::inst1");
   print(":::inst1");
}

private static void print(String cookie) {
   String[] params = cookie.split(":");
   System.out.print(cookie + " => ");
   for (String str : params) {
      System.out.print("<" + str + "> ");
   }
   System.out.println();
}

}
