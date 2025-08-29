import org.wnn.portal.pub.controller.req.IdempotentTokenCreateDTO;

import java.util.Optional;

/**
 * @author NanNan Wang
 */
public class Test {

    public static void main(String[] args) {
        IdempotentTokenCreateDTO request = new IdempotentTokenCreateDTO();
        long expireSeconds = Optional.ofNullable(request.getExpireSeconds())
                .filter(seconds -> seconds > 0)
                .orElse(5L);
        System.out.println(String.valueOf(expireSeconds));
    }
}
