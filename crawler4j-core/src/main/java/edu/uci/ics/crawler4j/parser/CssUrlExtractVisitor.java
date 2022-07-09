package edu.uci.ics.crawler4j.parser;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import com.helger.css.decl.CSSDeclaration;
import com.helger.css.decl.CSSExpressionMemberTermURI;
import com.helger.css.decl.CSSImportRule;
import com.helger.css.decl.ICSSTopLevelRule;
import com.helger.css.decl.visit.DefaultCSSUrlVisitor;

import crawlercommons.filters.basic.BasicURLNormalizer;
import edu.uci.ics.crawler4j.url.UrlResolver;

public class CssUrlExtractVisitor extends DefaultCSSUrlVisitor {
	
	private final String referenceAbsoluteUrl;
	private final BasicURLNormalizer normalizer;
	private final Set<String> seedUrls = new LinkedHashSet<>();
	
	
	public CssUrlExtractVisitor(final String referenceAbsoluteUrl, final BasicURLNormalizer normalizer) {
		this.referenceAbsoluteUrl = referenceAbsoluteUrl;
		this.normalizer = normalizer;
	}
	
	
	
	@Override
	public void onImport(@Nonnull final CSSImportRule aImportRule) {
		addSeedUrl(aImportRule.getLocationString());
	}
	
	
	@Override
	public void onUrlDeclaration(@Nullable final ICSSTopLevelRule aTopLevelRule,
			@Nonnull final CSSDeclaration aDeclaration, @Nonnull final CSSExpressionMemberTermURI aURITerm)
	{
		addSeedUrl(aURITerm.getURIString());
	}
	
	
	
	/**
	 * @return the added seed url or empty
	 */
	protected Optional<String> addSeedUrl(final String url) {
		if (StringUtils.isBlank(url) || url.startsWith("data:")) {
			return Optional.empty();
		}
		
		final String seedUrl = toSeedUrl(url);
		seedUrls.add(seedUrl);
		return Optional.of(seedUrl);
	}
	
	private String toSeedUrl(final String url) {
		// Normalization is needed, because the String will be input for URI.create(...).
		return normalizer.filter(UrlResolver.resolveUrl((referenceAbsoluteUrl == null) ? "" : referenceAbsoluteUrl, url));
	}
	
	
	
	public String getReferenceAbsoluteUrl() {
		return referenceAbsoluteUrl;
	}
	
	public BasicURLNormalizer getNormalizer() {
		return normalizer;
	}
	
	public Set<String> getSeedUrls() {
		return Collections.unmodifiableSet(seedUrls);
	}
}
