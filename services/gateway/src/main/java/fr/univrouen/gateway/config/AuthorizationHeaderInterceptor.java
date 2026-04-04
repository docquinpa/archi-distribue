package fr.univrouen.gateway.config;

import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.graphql.server.WebGraphQlRequest;
import org.springframework.graphql.server.WebGraphQlResponse;
import org.springframework.stereotype.Component;
import org.springframework.graphql.server.WebGraphQlInterceptor;
import reactor.core.publisher.Mono;

@Component
public class AuthorizationHeaderInterceptor implements WebGraphQlInterceptor {

    @Override
    public Mono<WebGraphQlResponse> intercept(WebGraphQlRequest request, Chain chain) {
        String authorizationHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        request.configureExecutionInput((executionInput, builder) ->
                builder.graphQLContext(Map.of("Authorization", authorizationHeader == null ? "" : authorizationHeader)).build()
        );

        return chain.next(request);
    }
}
