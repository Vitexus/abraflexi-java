package com.adleritech.flexibee.core.api.domain;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Root(strict = false)
public class Result {

    @Element(name = "id", required = false)
    private String id;

    @Element(name = "ref", required = false)
    private String ref;

    @ElementList(name = "errors", required = false)
    private List<Error> errors;

}
