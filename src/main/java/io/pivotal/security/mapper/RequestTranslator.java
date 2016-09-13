package io.pivotal.security.mapper;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.Option;
import io.pivotal.security.util.StringUtil;
import io.pivotal.security.view.ParameterizedValidationException;
import static com.google.common.collect.Lists.newArrayList;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.jayway.jsonpath.JsonPath.using;

public interface RequestTranslator<ET> {
  void populateEntityFromJson(ET namedSecret, DocumentContext documentContext);

  Set<String> getValidKeys();

  default void validateJsonKeys(DocumentContext parsed) {
    Set<String> keys = getValidKeys();
    Configuration conf = Configuration.builder().options(Option.AS_PATH_LIST).build();
    List<String> pathList = using(conf).parse(parsed.jsonString()).read("$..*");
    pathList = pathList.stream().map(StringUtil::convertJsonArrayRefToWildcard).collect(Collectors.toList());
    for (String path: pathList) {
      if(!keys.contains(path)) {
        throw new ParameterizedValidationException("error.invalid_json_key", newArrayList(path));
      }
    }
  }
}
