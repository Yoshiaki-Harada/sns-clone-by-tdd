package domain.model.user

import com.harada.domain.model.message.LIMIT_TWEET_LENGTH
import createTweet
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TweetTest {
    @Test
    public fun `Tweetの文字数が制限以内であることを確認できる`() {
        assertTrue(createTweet(text = "1".repeat(LIMIT_TWEET_LENGTH)).isInLimitTweetLength())
        assertFalse(createTweet(text = "1".repeat(LIMIT_TWEET_LENGTH+1)).isInLimitTweetLength())
    }
}