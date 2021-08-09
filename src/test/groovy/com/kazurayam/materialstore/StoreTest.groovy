package com.kazurayam.materialstore

import com.kazurayam.materialstore.selenium.AShotWrapper
import io.github.bonigarcia.wdm.WebDriverManager
import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.openqa.selenium.By
import org.openqa.selenium.Dimension
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions

import java.awt.image.BufferedImage
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.TimeUnit

import static org.junit.jupiter.api.Assertions.assertTrue

class StoreTest {

    private static Path outputDir =
            Paths.get(".").resolve("build/tmp/testOutput")
                    .resolve(StoreTest.class.getName())

    private static Path resultsDir =
            Paths.get(".").resolve("src/test/resources/fixture/sample_results")

    @BeforeAll
    static void beforeAll() {
        if (Files.exists(outputDir)) {
            FileUtils.deleteDirectory(outputDir.toFile())
        }
        Files.createDirectories(outputDir)
    }

    @Test
    void test_twins_mode() {
        Path root = outputDir.resolve("Materials")
        Store store = new StoreImpl(root)
        JobName jobName = new JobName("test_twins_mode")
        TestFixtureUtil.setupFixture(store, jobName)
        JobTimestamp jobTimestamp = JobTimestamp.now()

        // open the Chrome browser
        WebDriver driver = createChromeDriver()

        // visit the 1st page to take screenshot and save HTML source
        String profile1 = "ProductionEnv"
        doWebAction(driver, store, jobName, jobTimestamp,
                profile1,
                new URL("http://demoaut.katalon.com/"))

        // visit the 2nd page to take screenshot and save HTML source
        String profile2 = "DevelopmentEnv"
        doWebAction(driver, store, jobName, jobTimestamp,
                profile2,
                new URL("http://demoaut-mimic.kazurayam.com/"))

        // close the Chrome browser
        driver.quit()

        // pickup the materials that belongs to the 2 "profiles"
        List<Material> left = store.select(jobName, jobTimestamp,
                new MetadataPattern.Builder([ "profile": profile1 ]).build())

        List<Material> right = store.select(jobName, jobTimestamp,
                new MetadataPattern.Builder(["profile": profile2 ]).build())

        // make diff
        DiffArtifacts stuffedDiffArtifacts =
                store.makeDiff(left, right,
                        new MetadataIgnoredKeys.Builder()
                                .ignoreKey("profile")
                                .ignoreKey("URL")
                                .ignoreKey("URL.host")
                                .build())
        int warnings = stuffedDiffArtifacts.countWarnings(0.0d)
        println "found ${warnings} differences"

        // compile HTML report
        Path reportFile = store.reportDiffs(jobName, stuffedDiffArtifacts,
                0.0d,"index.html")
        assertTrue(Files.exists(reportFile))
    }

    /**
     *
     * @param driver
     * @param store
     * @param jobName
     * @param jobTimestamp
     * @param profile
     * @param url
     * @return
     */
    private static Tuple doWebAction(WebDriver driver,
                                     Store store,
                                     JobName jobName,
                                     JobTimestamp jobTimestamp,
                                     String profile,
                                     URL url) {
        // visit the page
        driver.navigate().to(url.toString())

        // take and store the PNG screenshot of the entire page
        BufferedImage entirePageImage = AShotWrapper.takeEntirePageImage(driver)
        Material material1 = store.write(jobName, jobTimestamp,
                FileType.PNG,
                new MetadataImpl.Builder(url)
                        .put("category", "screenshot")
                        .put("profile", profile)
                        .put("xpath", "/html")
                        .build(),
                entirePageImage)
        assert material1 != null

        // take and store the PNG screenshot of the button element
        String xpath = "//a[@id='btn-make-appointment']"
        BufferedImage elementImage = AShotWrapper.takeWebElementImage(driver, By.xpath(xpath))
        Material material2 = store.write(jobName, jobTimestamp,
                FileType.PNG,
                new MetadataImpl.Builder(url)
                        .put("category", "screenshot")
                        .put("profile", profile)
                        .put("xpath", xpath)
                        .build(),
                elementImage)
        assert material2 != null


        // get and store the HTML page source of the page
        String html = driver.getPageSource()
        Material material3 = store.write(jobName, jobTimestamp,
                FileType.HTML,
                new MetadataImpl.Builder(url)
                        .put("category", "page source")
                        .put("profile", profile)
                        .put("xpath", "/html")
                        .build(),
                html,
                StandardCharsets.UTF_8)
        assert material3 != null

        return new Tuple(material1, material2, material3)
    }

    static WebDriver createChromeDriver() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--headless");
        WebDriver driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(120, TimeUnit.MILLISECONDS);
        driver.manage().window().setSize(new Dimension(800, 800));
        return driver
    }
}