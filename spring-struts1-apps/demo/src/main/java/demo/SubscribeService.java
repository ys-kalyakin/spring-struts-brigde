package demo;

import org.springframework.stereotype.Service;

/**
 * A plain Spring service of the demo application: the annotated action
 * receives it through dependency injection.
 */
@Service
public class SubscribeService {

    public String subscribe(String name, String email) {
        return "Subscribed " + name + " <" + email + ">";
    }
}
