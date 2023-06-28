package com.kkaebom.core.crawling

import com.kkaebom.core.properties.InstagramProperties
import com.kkaebom.db.crawling.entity.VolunteerRecruitmentCrawling
import com.kkaebom.db.crawling.entity.VolunteerRecruitmentCrawlingData
import io.github.bonigarcia.wdm.WebDriverManager
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.openqa.selenium.*
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.time.Duration


@Component
class WebCrawler @Autowired constructor(val instagramProperty: InstagramProperties) {
    lateinit var driver: WebDriver
    lateinit var wait: WebDriverWait
    lateinit var shortWait: WebDriverWait
    private val log: Logger = LoggerFactory.getLogger(WebCrawler::class.java)

    @PostConstruct
    fun init() {
        WebDriverManager.chromedriver().setup()
        val options = ChromeOptions()
        options.addArguments("--disable-popup-blocking");       //팝업안띄움
        options.addArguments("headless");                       //브라우저 안띄움
        options.addArguments("--disable-gpu");            //gpu 비활성화
        options.addArguments("--blink-settings=imagesEnabled=false"); //이미지 다운 안받음

        val chromiumDriver = ChromeDriver(options)
        val executeCdpCommand: MutableMap<String, Any> = HashMap()
        executeCdpCommand["source"] = "Object.defineProperty(navigator, 'webdriver', { get: () => undefined })"
        chromiumDriver.executeCdpCommand("Page.addScriptToEvaluateOnNewDocument", executeCdpCommand)
        driver = chromiumDriver
        wait = WebDriverWait(driver, Duration.ofSeconds(5))
        shortWait = WebDriverWait(driver, Duration.ofSeconds(2))
    }

    @PreDestroy
    fun destroy() {
        driver.quit()
    }

    fun branchCrawling(type: VolunteerRecruitmentCrawlingType): List<VolunteerRecruitmentCrawling> {
        if (driver.windowHandles.isEmpty()) {
            driver.switchTo().newWindow(WindowType.TAB)
        }

        while (driver.windowHandles.count() > 1) {
            driver.close()
        }

        return when (type) {
            VolunteerRecruitmentCrawlingType.SITE_1365 -> site1365Crawling()
            VolunteerRecruitmentCrawlingType.ZOOSEYO -> zooseyoCrawling()
            VolunteerRecruitmentCrawlingType.NAVER_CAFE_SEOUL_ANIMAL_CARE -> naverCafeSeoulanimalcareCrawling()
            VolunteerRecruitmentCrawlingType.INSTAGRAM -> instagramChannelCrawling()
        }
    }

    fun site1365Crawling(): List<VolunteerRecruitmentCrawling> {
        val site1365 = "https://www.1365.go.kr/vols/search.do?"
        val query = "realQuery=유기견&query=유기견&"
        val personal = "collection=personalserve&"
        val sort = "sort=RANK&"
        val searchField = "searchField=ALL&"

        driver.get(site1365 + query + personal + sort + searchField)
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10))

        wait.until(
            ExpectedConditions.presenceOfElementLocated(By.className("search_group"))
        )

        val originalWindow = driver.windowHandle
        val volunteerList =
            driver.findElement(By.cssSelector("#integrated_search > div.content_view > div.search_wrap > div.search_left > div > div.search_box"))

        val crawlings = mutableListOf<VolunteerRecruitmentCrawling>()
        volunteerList.findElements(By.className("tit_g")).forEach {
            it.findElements(By.className("tit")).forEach {
                val href = it.getAttribute("href")

                closeWithoutOriginalWindow(originalWindow)
                driver.switchTo().newWindow(WindowType.TAB).get(href)
                val entity = VolunteerRecruitmentCrawling(
                    url = href,
                    site = "www.1365.go.kr"
                )

                alertCheck(originalWindow)

                try {
                    driver.findElements(By.className("tit_board_view")).forEach {
                        addData(entity, "title", it.text)
                    }
                } catch (_: Exception) {
                }

                driver.findElements(By.cssSelector("#content > div.content_view > div > div.board_view.type2 > div.board_data.type2"))
                    .forEach {
                        try {
                            it.findElements(By.className("group")).forEach {
                                addData(
                                    entity,
                                    it.findElement(By.tagName("dt")).text,
                                    it.findElement(By.tagName("dd")).text
                                )
                            }
                        } catch (_: Exception) {
                        }
                    }

                try {
                    addData(
                        entity,
                        "content",
                        driver.findElement(By.cssSelector("#content > div.content_view > div > div.board_view.type2 > div.board_body > div.bb_txt > pre")).text
                    )
                } catch (_: Exception) {
                }

                try {
                    driver.findElement(By.cssSelector("#content > div.content_view > div > div.board_view.type2 > div.board_body > div.incharge_data"))
                        .findElements(By.tagName("dl")).forEach {
                            addData(
                                entity,
                                it.findElement(By.tagName("dt")).text,
                                it.findElement(By.tagName("dd")).text
                            )
                        }
                    crawlings.add(entity)
                } catch (_: Exception) {
                }

                driver.close()
                driver.switchTo().window(originalWindow)
            }
        }
        return crawlings
    }

    private fun alertCheck(originalWindow: String?) {
        try {
            val alert = wait.until(ExpectedConditions.alertIsPresent())
            alert.dismiss()
            driver.close()
            driver.switchTo().window(originalWindow)
        } catch (_: Exception) {
        }
    }

    fun zooseyoCrawling(): List<VolunteerRecruitmentCrawling> {
        val url = "https://www.zooseyo.or.kr/Yu_board/volunteer_listV.php"
        val site = "www.zooseyo.or.kr"
        val selector =
            "body > table > tbody > tr > td > table:nth-child(8) > tbody > tr > td:nth-child(3) > table > tbody > tr > td > table > tbody > tr:nth-child(2) > td > table > tbody > tr:nth-child(4) > td > table > tbody > tr > td > table > tbody > tr:nth-child(2) > td > table > tbody > tr > td:nth-child(2) > table > tbody > tr:nth-child(1) > td"
        driver.get(url)
        val originalWindow = driver.windowHandle
        val homeTable = driver.findElement(By.cssSelector(selector))

        val crawlings = mutableListOf<VolunteerRecruitmentCrawling>()
        homeTable.findElements(By.cssSelector("table > tbody > tr > td > a")).forEach {
            closeWithoutOriginalWindow(originalWindow)
            it.click()

            val isNewWindow = wait.until(ExpectedConditions.numberOfWindowsToBe(2))
            if (!isNewWindow) return@forEach

            driver.windowHandles.filter { !it.equals(originalWindow) }.forEach {
                driver.switchTo().window(it)
                val crawling = VolunteerRecruitmentCrawling(
                    url = driver.currentUrl,
                    site = site,
                )

                try {
                    val table =
                        driver.findElement(By.cssSelector("body > table > tbody > tr > td > table > tbody > tr:nth-child(2) > td > table > tbody > tr:nth-child(4) > td > table > tbody"))
                    val title =
                        table.findElement(By.cssSelector("tr:nth-child(2) > td > table > tbody > tr > td:nth-child(2)")).text
                    val shelterName =
                        table.findElement(By.cssSelector("tr:nth-child(4) > td > table > tbody > tr > td:nth-child(2)")).text
                    val address =
                        table.findElement(By.cssSelector("tr:nth-child(4) > td > table > tbody > tr > td:nth-child(4)")).text
                    val name =
                        table.findElement(By.cssSelector("tr:nth-child(6) > td > table > tbody > tr > td:nth-child(2)")).text
                    val registerName =
                        table.findElement(By.cssSelector("tr:nth-child(6) > td > table > tbody > tr > td:nth-child(4)")).text
                    val number =
                        table.findElement(By.cssSelector("tr:nth-child(8) > td > table > tbody > tr > td:nth-child(2)")).text
                    val phoneNumber =
                        table.findElement(By.cssSelector("tr:nth-child(8) > td > table > tbody > tr > td:nth-child(4)")).text
                    val homePage =
                        table.findElement(By.cssSelector("tr:nth-child(10) > td > table > tbody > tr > td:nth-child(2)")).text
                    val recruitmentPeriod =
                        table.findElement(By.cssSelector("tr:nth-child(12) > td > table > tbody > tr > td:nth-child(2)")).text
                    val volunteerPeriod =
                        table.findElement(By.cssSelector("tr:nth-child(12) > td > table > tbody > tr > td:nth-child(4)")).text
                    val day =
                        table.findElement(By.cssSelector("tr:nth-child(14) > td > table > tbody > tr > td:nth-child(2)")).text
                    val content =
                        table.findElement(By.cssSelector("tr:nth-child(19) > td > table > tbody > tr > td:nth-child(2)")).text
                    addData(crawling, "Title", title)
                    addData(crawling, "단체 및 시설이름", shelterName)
                    addData(crawling, "주소", address)
                    addData(crawling, "담당자 이름", name)
                    addData(crawling, "등록인", registerName)
                    addData(crawling, "단체,시설 연락처", number)
                    addData(crawling, "휴대폰번호", phoneNumber)
                    addData(crawling, "홈페이지", homePage)
                    addData(crawling, "모집기간", recruitmentPeriod)
                    addData(crawling, "봉사활동기간", volunteerPeriod)
                    addData(crawling, "활동가능요일", day)
                    addData(crawling, "내용", content)
                } catch (_: Exception) {
                }

                crawlings.add(crawling)
                driver.close()
            }

            driver.switchTo().window(originalWindow)
        }

        return crawlings
    }

    fun naverCafeSeoulanimalcareCrawling(): List<VolunteerRecruitmentCrawling> {
        val url =
            "https://cafe.naver.com/seoulanimalcare?iframe_url=/ArticleList.nhn%3Fsearch.clubid=29193247%26search.menuid=14%26search.boardtype=L"
        driver.get(url)
        val originalWindow = driver.windowHandle
        val cafeMainFrame = "cafe_main"

        driver.switchTo().frame(cafeMainFrame)
        val titles = driver.findElement(By.cssSelector("#main-area > div:nth-child(4) > table > tbody"))
            .findElements(By.className("board-list"))

        val crawlings = mutableListOf<VolunteerRecruitmentCrawling>()
        titles.forEach {
            val aTag = it.findElement(By.tagName("a"))
            val href = aTag.getAttribute("href")

            val crawling = VolunteerRecruitmentCrawling(
                url = href,
                site = "https://cafe.naver.com/seoulanimalcare",
            )

            closeWithoutOriginalWindow(originalWindow)
            driver.switchTo().newWindow(WindowType.TAB).get(href)

            if (driverWaitFailForNaverCafe(By.id(cafeMainFrame), cafeMainFrame, originalWindow)) return@forEach

            driver.switchTo().frame(driver.findElement(By.id(cafeMainFrame)))
            if (driverWaitFailForNaverCafe(
                    By.className("se-module-text"),
                    cafeMainFrame,
                    originalWindow
                )
            ) return@forEach

            try {
                addData(crawling, "content", driver.findElements(By.className("se-module-text")).first().text)
            } catch (_: Exception) {
            }
            crawlings.add(crawling)
            backOriginWindowWithFrame(originalWindow, cafeMainFrame)
        }

        return crawlings
    }

    private fun driverWaitFailForNaverCafe(by: By, frame: String, originalWindow: String): Boolean {
        try {
            driverWaitElement(by)
        } catch (_: TimeoutException) {
            if (driver.windowHandles.count() == 3) {
                driver.switchTo().window(driver.windowHandles.last())
                driver.close()
                driver.switchTo().window(driver.windowHandles.last())
            }
            backOriginWindowWithFrame(originalWindow, frame)
            return true
        }
        return false
    }

    private fun backOriginWindowWithFrame(originalWindow: String?, frame: String) {
        driver.close()
        driver.switchTo().window(originalWindow)
        driver.switchTo().frame(frame)
    }

    fun instagramChannelCrawling(): List<VolunteerRecruitmentCrawling> {
        val url = "https://www.instagram.com/"
        instagramLogin(url)

        val crawlings = mutableListOf<VolunteerRecruitmentCrawling>()
        val originalWindow = driver.windowHandle
        instagramProperty.channels.forEach {
            val channelUrl = url + it
            driver.get(channelUrl)

            try {
                driverWaitElement(By.tagName("article"))
                    .findElements(By.tagName("a")).forEach {
                        val href = it.getAttribute("href")

                        closeWithoutOriginalWindow(originalWindow)
                        driver.switchTo().newWindow(WindowType.TAB)
                        driver.get(href)
                        val crawling = VolunteerRecruitmentCrawling(
                            url = href,
                            site = channelUrl,
                        )

                        try {
                            val content = driverWaitElement(By.tagName("h1")).text
                            addData(crawling, "content", content)
                        } catch (_: TimeoutException) {
                        }

                        crawlings.add(crawling)
                        driver.close()
                        driver.switchTo().window(originalWindow)
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return crawlings
    }

    fun instagramLogin(url: String) {
        driver.get(url)

        try {
            driverWaitElement(By.cssSelector("#loginForm > div > div:nth-child(1) > div > label > input"))
                .sendKeys(instagramProperty.loginId)
            driverWaitElement(By.cssSelector("#loginForm > div > div:nth-child(2) > div > label > input"))
                .sendKeys(instagramProperty.password)
            driverWaitElement(By.cssSelector("#loginForm > div > div:nth-child(3) > button")).click()

            wait.until(
                ExpectedConditions.stalenessOf(
                    driver.findElement(
                        By.cssSelector("#loginForm > div > div:nth-child(3) > button")
                    )
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun driverWaitElement(by: By): WebElement {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(by))
    }

    fun addData(crawling: VolunteerRecruitmentCrawling, title: String, content: String) {
        if (content.isBlank()) return
        crawling.addData(
            VolunteerRecruitmentCrawlingData(
                title = title,
                content = content
            )
        )
    }

    fun closeWithoutOriginalWindow(originalWindow: String) {
        driver.windowHandles.forEach {
            if (!driver.windowHandle.equals(originalWindow)) {
                driver.switchTo().window(it)
                driver.close()
            }
        }
    }
}