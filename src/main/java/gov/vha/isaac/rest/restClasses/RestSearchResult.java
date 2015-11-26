package gov.vha.isaac.rest.restClasses;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class RestSearchResult
{
	/**
	 * The internal identifier of the description sememe that matched the query
	 */
	@XmlElement int matchNid;
	/**
	 * The text of the description that matched the query (may be blank, if the description is not available/active on the path used to populate this)
	 */
	@XmlElement String matchText;
	/**
	 * The Lucene Score for this result.  This value is only useful for ranking search results relative to other search results within the SAME QUERY 
	 * execution.
	 */
	@XmlElement float score;

	protected RestSearchResult()
	{
		//for Jaxb
	}

	public RestSearchResult(int matchNid, String matchText, float score)
	{
		this.matchNid = matchNid;
		this.matchText = matchText;
		this.score = score;
	}
}
