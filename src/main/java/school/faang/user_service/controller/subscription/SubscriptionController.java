package school.faang.user_service.controller.subscription;

import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import school.faang.user_service.service.subscription.SubscriptionService;

import java.util.Map;

@RestController
@RequestMapping("subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping("/follow")
    public Map.Entry<String, Boolean> followUser(@PathParam("followerId") long followerId,
                                                 @PathParam("followeeId") long followeeId) {
        subscriptionService.followUser(followerId, followeeId);
        return Map.entry("isFollowed", true);
    }

    @DeleteMapping("/unfollow")
    public Map.Entry<String, Boolean> unfollowUser(@PathParam("followerId") long followerId,
                                                   @PathParam("followeeId") long followeeId) {
        subscriptionService.unfollowUser(followerId, followeeId);
        return Map.entry("isUnfollowed", true);
    }

    @GetMapping("/count/{followerId}")
    public Map.Entry<String, Integer> getFollowersCount(@PathVariable("followerId") long followerId) {
        int followeesCount = subscriptionService.getFollowersCount(followerId);
        return Map.entry("followeesCount", followeesCount);
    }


}
