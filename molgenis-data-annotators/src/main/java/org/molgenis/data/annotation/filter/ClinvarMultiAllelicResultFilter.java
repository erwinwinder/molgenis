package org.molgenis.data.annotation.filter;

import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;

import org.apache.lucene.queries.function.valuesource.MultiFunction.Values;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.entity.ResultFilter;
import org.molgenis.data.vcf.VcfRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClinvarMultiAllelicResultFilter implements ResultFilter
{

	private List<AttributeMetaData> attributes;

	public ClinvarMultiAllelicResultFilter(List<AttributeMetaData> attributes)
	{
		this.attributes = attributes;
	}

	@Override
	public Collection<AttributeMetaData> getRequiredAttributes()
	{
		return Arrays.asList(VcfRepository.REF_META, VcfRepository.ALT_META);
	}

	@Override
	public Optional<Entity> filterResults(Iterable<Entity> results, Entity annotatedEntity)
	{
		Map<String, String> clnallValueMap = new HashMap<>();
		Map<String, String> clnsigValueMap = new HashMap<>();
		List<Entity> processedResults = new ArrayList<>();

		for (Entity entity : results)
		{
			if (entity.get(VcfRepository.REF).equals(annotatedEntity.get(VcfRepository.REF)))
			{
				String[] alts = entity.getString(VcfRepository.ALT).split(",");
				String[] clnSigs = entity.getString("INFO_CLNSIG").split(",");
				String[] clnAll = entity.getString("INFO_CLNALLE").split(",");

				StringBuilder newClnlallAttributeValue = new StringBuilder();
				StringBuilder newClnlsigAttributeValue = new StringBuilder();
				String[] annotatedEntityAltAlleles = annotatedEntity.getString(VcfRepository.ALT).split(",");
				// sometimes the clnsig is not defined for all alternative alleles
				// so we need to check this and just add what we have
				for (int i = 0; i < clnSigs.length; i++)
				{
					int significantAlleleIndex = Integer.parseInt(clnAll[i]);

					// this means the no allele is associated with the gene of interest
					if (significantAlleleIndex == -1) continue;

					// this means the allele is based on the reference
					else if (significantAlleleIndex == 0)
					{
						String refAllele = annotatedEntity.getString(VcfRepository.REF);

						for (String annotatedEntityAltAllele : annotatedEntityAltAlleles)
						{
							// if annotated entity allele equals the clinvar significant allele we want it!
							if (refAllele.equals(annotatedEntityAltAllele))
							{
								clnallValueMap.put(refAllele, clnAll[i]);
								clnsigValueMap.put(refAllele, clnSigs[i]);
							}
						}
					}
					// 1 based so we need subtract 1 from the clnAll value
					else
					{
						significantAlleleIndex = significantAlleleIndex - 1;

						for (String annotatedEntityAltAllele : annotatedEntityAltAlleles)
						{
							// if annotated entity allele equals the clinvar significant allele we want it!
							if (alts[significantAlleleIndex].equals(annotatedEntityAltAllele))
							{
								clnallValueMap.put(alts[significantAlleleIndex], clnAll[i]);
								clnsigValueMap.put(alts[significantAlleleIndex], clnSigs[i]);
							}
						}
					}

				}

				for (int i = 0; i < annotatedEntityAltAlleles.length; i++)
				{
					if (i != 0)
					{
						newClnlallAttributeValue.append(",");
					}
					if (clnallValueMap.get(annotatedEntityAltAlleles[i]) != null)
					{
						newClnlallAttributeValue.append(clnallValueMap.get(annotatedEntityAltAlleles[i]));
					}
					else
					{
						// missing allele in source, add a dot
						newClnlallAttributeValue.append(".");
					}
					if (clnsigValueMap.get(annotatedEntityAltAlleles[i]) != null)
					{
						newClnlsigAttributeValue.append(clnsigValueMap.get(annotatedEntityAltAlleles[i]));
					}
					else
					{
						// missing allele in source, add a dot
						newClnlsigAttributeValue.append(".");
					}
				}
				// nothing found at all? result is empty
				if (newClnlallAttributeValue.toString().equals("."))
				{
					entity.set("INFO_CLNSIG", "");
					entity.set("INFO_CLNALLE", "");
				}
				else
				{
					entity.set("INFO_CLNALLE", newClnlallAttributeValue.toString());
					entity.set("INFO_CLNSIG", newClnlsigAttributeValue.toString());

				}
				processedResults.add(entity);
			}

		}

		return FluentIterable.from(processedResults).first();
	}
}