/*
 * Copyright (C) 2014 Atlas of Living Australia
 * All Rights Reserved.
 *
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 */

package au.org.ala.biocache.hubs

import org.apache.commons.lang.StringUtils
import org.codehaus.groovy.grails.web.json.JSONObject

/**
 * Service to cache the facet values available from a given data hub.
 * Used to populate the values in select drop-down lists in advanced search page.
 */
class FacetsCacheService {
    def webServicesService
    Map facetsMap = [:]

    def Map getFacetNamesFor(FacetsName facet) {
        if (!facetsMap) {
            loadSearchResults()
        }

        return facetsMap?.get(facet.fieldname)
    }

    def clearCache() {
        facetsMap = [:]
    }

    private void loadSearchResults() {
        SpatialSearchRequestParams requestParams = new SpatialSearchRequestParams()
        requestParams.setQ("*:*")
        requestParams.setPageSize(0)
        requestParams.setFlimit(-1)
        requestParams.setFacets(FacetsName.values().collect{ it.fieldname } as String[])
        JSONObject sr = webServicesService.cachedFullTextSearch(requestParams)

        sr.facetResults.each { fq ->
            def fieldName = fq.fieldName
            def fields = [:]
            fq.fieldResult.each {
                if (it.fq) {
                    def values = it.fq.tokenize(":")
                    def value = StringUtils.remove(values[1], '"') // some values have surrounding quotes
                    fields.put(value, it.label)
                } else {
                    fields.put(it.label, it.label)
                }
            }

            facetsMap.put(fieldName, fields)
        }
    }

}