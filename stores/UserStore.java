import java.util.List;

public interface UserStore {
    void add(User user);

    User findByUsername(String username);

    boolean delete(String username);

    List<User> listAll();

    boolean hasAnyUsers();
}

