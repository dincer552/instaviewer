package com.dincer552.instaviewer.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LinkValidatorTest {
    @Test fun validInstagramLink() = assertTrue(LinkValidator.isValidLink("https://www.instagram.com/reel/abc123/"))
    @Test fun validYoutubeLink() = assertTrue(LinkValidator.isValidLink("https://www.youtube.com/watch?v=abc123"))
    @Test fun validShortYoutubeLink() = assertTrue(LinkValidator.isValidLink("https://youtu.be/abc123"))
    @Test fun validXLink() = assertTrue(LinkValidator.isValidLink("https://x.com/user/status/123"))
    @Test fun validTwitterLink() = assertTrue(LinkValidator.isValidLink("https://twitter.com/user/status/123"))
    @Test fun validTiktokLink() = assertTrue(LinkValidator.isValidLink("https://www.tiktok.com/@user/video/123"))
    @Test fun validRedditLink() = assertTrue(LinkValidator.isValidLink("https://www.reddit.com/r/android/comments/abc/"))
    @Test fun validFacebookLink() = assertTrue(LinkValidator.isValidLink("https://www.facebook.com/watch/?v=123"))
    @Test fun validThreadsLink() = assertTrue(LinkValidator.isValidLink("https://www.threads.net/@user/post/abc"))
    @Test fun validVimeoLink() = assertTrue(LinkValidator.isValidLink("https://vimeo.com/123456789"))
    @Test fun validBlueskyLink() = assertTrue(LinkValidator.isValidLink("https://bsky.app/profile/user/post/abc"))
    @Test fun validDirectVideoLink() = assertTrue(LinkValidator.isValidLink("https://example.com/video/clip.mp4"))
    @Test fun invalidUnknownHost() = assertFalse(LinkValidator.isValidLink("https://example.com/page"))
    @Test fun invalidHttpScheme() = assertFalse(LinkValidator.isValidLink("http://www.instagram.com/reel/abc/"))
    @Test fun invalidPlainText() = assertFalse(LinkValidator.isValidLink("not a link at all"))
    @Test fun nullIsInvalid() = assertFalse(LinkValidator.isValidLink(null))

    @Test fun extractUrlFromShareText() {
        assertEquals("https://www.instagram.com/reel/abc123/", LinkValidator.extractUrl("Check this out https://www.instagram.com/reel/abc123/?dmpr=tracking== via Instagram"))
    }
    @Test fun instagramDmprShareLinkIsCanonicalized() {
        assertEquals("https://www.instagram.com/reel/Df1ipAQNty9/", LinkValidator.normalizeUrl("https://www.instagram.com/reel/Df1ipAQNty9/?dmpr=MW1wbTh2aTFwMjVncg=="))
    }
    @Test fun instagramTrackingParametersAreRemoved() {
        assertEquals("https://www.instagram.com/reel/abc123/", LinkValidator.normalizeUrl("https://instagram.com/reel/abc123/?igsh=abc&igshid=def&utm_source=share"))
    }
    @Test fun instagramCarouselIndexIsPreserved() {
        assertEquals("https://www.instagram.com/p/abc123/?img_index=2", LinkValidator.normalizeUrl("https://www.instagram.com/p/abc123/?dmpr=abc&img_index=2"))
    }
    @Test fun youtubeShareTrackingIsRemovedAndVideoIdIsExtracted() {
        val url = LinkValidator.normalizeUrl("https://youtu.be/aqz-KE-bpKQ?si=tracking")
        assertEquals("https://youtu.be/aqz-KE-bpKQ", url)
        assertEquals("aqz-KE-bpKQ", LinkValidator.youtubeVideoId(url!!))
    }
    @Test fun youtubeShortsVideoIdIsExtracted() {
        assertEquals("aqz-KE-bpKQ", LinkValidator.youtubeVideoId("https://www.youtube.com/shorts/aqz-KE-bpKQ?feature=share"))
    }
    @Test fun twitterLinksAreCanonicalizedToX() {
        assertEquals("https://x.com/user/status/123", LinkValidator.normalizeUrl("https://twitter.com/user/status/123?s=20"))
    }
    @Test fun malformedUrlDoesNotCrash() = assertNull(LinkValidator.normalizeUrl("https://[broken"))
    @Test fun directVideoUrlDetectionIgnoresQueryString() {
        assertTrue(LinkValidator.isDirectMediaUrl("https://cdn.example.com/video.mp4?token=abc"))
    }
    @Test fun detectPlatformInstagram() = assertEquals("Instagram", LinkValidator.detectPlatform("https://www.instagram.com/reel/abc/"))
    @Test fun detectPlatformYouTube() = assertEquals("YouTube", LinkValidator.detectPlatform("https://www.youtube.com/watch?v=abc"))
    @Test fun detectPlatformYouTubeShort() = assertEquals("YouTube", LinkValidator.detectPlatform("https://youtu.be/abc"))
    @Test fun detectPlatformX() = assertEquals("X / Twitter", LinkValidator.detectPlatform("https://x.com/user/status/1"))
    @Test fun detectPlatformTiktok() = assertEquals("TikTok", LinkValidator.detectPlatform("https://www.tiktok.com/@user/video/1"))
    @Test fun detectPlatformReddit() = assertEquals("Reddit", LinkValidator.detectPlatform("https://www.reddit.com/r/android/"))
    @Test fun detectPlatformFacebook() = assertEquals("Facebook", LinkValidator.detectPlatform("https://www.facebook.com/watch/?v=1"))
    @Test fun detectPlatformThreads() = assertEquals("Threads", LinkValidator.detectPlatform("https://www.threads.net/@user/post/abc"))
    @Test fun detectPlatformVimeo() = assertEquals("Vimeo", LinkValidator.detectPlatform("https://vimeo.com/123"))
    @Test fun detectPlatformBluesky() = assertEquals("Bluesky", LinkValidator.detectPlatform("https://bsky.app/profile/user/post/abc"))
    @Test fun detectPlatformDirectMedia() = assertEquals("Direct Media", LinkValidator.detectPlatform("https://example.com/video/clip.mp4"))
    @Test fun detectPlatformUnknown() = assertEquals("Unknown", LinkValidator.detectPlatform("https://example.com/page"))
}