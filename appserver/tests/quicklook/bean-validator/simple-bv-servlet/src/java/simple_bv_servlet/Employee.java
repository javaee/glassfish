package simple_bv_servlet;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

public class Employee implements Serializable {

  @Max(60)
  @Min(20)
  private int age;

  public void setAge(int age){
    this.age = age;
  }

  public int getAge(){
    return age;
  }

  @NotNull
  @Email
  private String email;

  public void setEmail(String email){
    this.email = email;
  }

  public String getEmail(){
    return email;
  }

  @NotNull
  private String firstName;

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  @NotNull
  private String lastName;

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  @NotNull
  private List<String> listOfString;

  public List<String> getListOfString() {
    return listOfString;
  }

  public void setListOfString(List<String> listOfString) {
    this.listOfString = listOfString;
  }
}
