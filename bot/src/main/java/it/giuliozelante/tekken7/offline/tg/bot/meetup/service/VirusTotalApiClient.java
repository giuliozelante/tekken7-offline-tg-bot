package it.giuliozelante.tekken7.offline.tg.bot.meetup.service;

import static io.micronaut.http.HttpHeaders.ACCEPT;
import static io.micronaut.http.HttpHeaders.CONTENT_TYPE;

import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.client.annotation.Client;
import it.giuliozelante.tekken7.offline.tg.bot.meetup.model.virustotal.Response;

@Header(name = ACCEPT, value = "application/json")
@Header(name = CONTENT_TYPE, value = "application/x-www-form-urlencoded")
@Header(name = "x-apikey", value = "${virustotal.api.key}")
@Client("${virustotal.api.root}")
public interface VirusTotalApiClient {

    @Post("urls")
    public Response scanUrl(String url);

    @Get("/analyses/{id}")
    public Response getUrlOrFileAnalysis(String id);
}