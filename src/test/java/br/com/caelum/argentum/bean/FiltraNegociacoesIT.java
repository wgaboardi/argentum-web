package br.com.caelum.argentum.bean;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.GenericArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.importer.ExplodedImporter;
import org.jboss.shrinkwrap.resolver.api.Resolvers;
import org.jboss.shrinkwrap.resolver.api.maven.ConfigurableMavenResolverSystem;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.MavenResolvedArtifact;
import org.jboss.shrinkwrap.resolver.api.maven.MavenResolverSystem;
import org.jboss.shrinkwrap.resolver.api.maven.ScopeType;
import org.jboss.shrinkwrap.resolver.api.maven.archive.importer.MavenImporter;
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenCoordinate;
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenDependencies;
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenDependency;
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenDependencyExclusion;
import org.jboss.shrinkwrap.resolver.api.maven.strategy.AcceptScopesStrategy;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

@RunWith(Arquillian.class)
public class FiltraNegociacoesIT {

	
	private static final SimpleDateFormat SDF = new SimpleDateFormat("ddMMyyyy");
	private static final String HTTP_LOCALHOST_8888 = "http://localhost:8080/argentum-web";
	private WebDriver driver;

	@Deployment
	public static WebArchive createWar() {

		
	
		File[] files = Maven.resolver().loadPomFromFile("pom.xml")
                                    .importDependencies(ScopeType.COMPILE, ScopeType.TEST, ScopeType.PROVIDED, ScopeType.RUNTIME)
                                    .resolve().withTransitivity().asFile();
				
		WebArchive webArchive = ShrinkWrap
				.create(WebArchive.class, "argentum-web.war")
				.addPackages(true, "br.com.caelum.argentum")
				.addAsLibraries(files)
				.as(ExplodedImporter.class)
				.importDirectory(new File("src/main/webapp"))
				.as(WebArchive.class);

		return webArchive;
	}

	@Before
	public void iniciaNavegador() {
		if (System.getProperty("phantomjs.binary.path") != null) {
			
			iniciaPhantomJs();
			
		} else if (System.getProperty("webdriver.chrome.driver") != null) {
			
			iniciaChrome();
			
		} else {
			
			throw new RuntimeException("Nao eh possivel determinar o navegador para execucao dos testes.");
			
		}
	}

	@After
	public void desligaNavegador() {
		driver.quit();
	}

	@Test
	public void verificaResultadosFiltradosNaTabela()
			throws InterruptedException {
		driver.navigate().to(HTTP_LOCALHOST_8888 + "/index.xhtml");

		List<WebElement> linhasDaTabelaDeNotificacoes = driver.findElements(By
				.xpath("//*[@id='tabelaNegociacoes']/div[2]/table/tbody/tr"));

		int totalDeNotificacoesAntesDoFiltro = linhasDaTabelaDeNotificacoes
				.size();

		WebElement filtroDataDe = driver.findElement(By.name("filtroDataDe"));
		WebElement filtroDataAte = driver.findElement(By.name("filtroDataAte"));

		filtroDataDe.click(); // especialmente para o inputMask
		filtroDataDe.sendKeys(SDF.format(hojeMenos10Dias()));

		filtroDataAte.click(); // especialmente para o inputMask
		filtroDataAte.sendKeys(SDF.format(hojeMais10Dias()));

		filtroDataAte.submit();

		WebElement botaoFiltro = driver.findElement(By.name("botaoFiltro"));

		botaoFiltro.click();

		Thread.sleep(2000L);

		linhasDaTabelaDeNotificacoes = driver.findElements(By
				.xpath("//*[@id='tabelaNegociacoes']/div[2]/table/tbody/tr"));

		int totalDeNotificacoesDepoisDoFiltroAplicado = linhasDaTabelaDeNotificacoes
				.size();

		Assert.assertTrue(totalDeNotificacoesDepoisDoFiltroAplicado < totalDeNotificacoesAntesDoFiltro);
	}

	private Date hojeMais10Dias() {
		Calendar data = Calendar.getInstance();
		data.add(Calendar.DAY_OF_MONTH, 10);
		return data.getTime();
	}

	private Date hojeMenos10Dias() {
		Calendar data = Calendar.getInstance();
		data.add(Calendar.DAY_OF_MONTH, -10);
		return data.getTime();
	}

	private void iniciaChrome() {
		driver = new ChromeDriver();
	}

	private void iniciaPhantomJs() {
		DesiredCapabilities dc = new DesiredCapabilities();
		dc.setJavascriptEnabled(true);
		driver = new PhantomJSDriver(dc);
	}
	
}
