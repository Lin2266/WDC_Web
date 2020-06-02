
package course.model;

public class Employee {
    private String name;
    private int id;
    private Address address;

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public Address getAddress() {
        return address;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setAddress(Address address) {
        this.address = address;
    }
    
}
