package top.kwseeker.rocketmq.example.model;

public class User {

    private String name;
    private Byte age;

    public String getName() {
        return name;
    }

    public User setName(String name) {
        this.name = name;
        return this;
    }

    public Byte getAge() {
        return age;
    }

    public User setAge(Byte age) {
        this.age = age;
        return this;
    }
}
