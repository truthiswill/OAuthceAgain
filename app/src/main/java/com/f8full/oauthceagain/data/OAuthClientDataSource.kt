package com.f8full.oauthceagain.data

import com.f8full.oauthceagain.data.model.RegisteredOAuthClient
import java.io.IOException
import com.nimbusds.oauth2.sdk.client.ClientRegistrationErrorResponse
import com.nimbusds.oauth2.sdk.client.ClientDeleteRequest
import com.nimbusds.oauth2.sdk.token.BearerAccessToken
import com.nimbusds.oauth2.sdk.util.JSONObjectUtils
import com.nimbusds.openid.connect.sdk.rp.*
import java.net.URI


/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class OAuthClientDataSource {

    fun register(cozyBaseUrlString: String): Result<RegisteredOAuthClient> {

        val jsonMetadata =
            "{\"redirect_uris\":[\"findmybikes://com.f8full.oauthceagain.oauth2redirect\"],\"client_name\":\"#findmybikes\",\"software_id\":\"github.com/f8full/findmybikesTRUC\",\"software_version\":\"999\",\"client_kind\":\"mobile\",\"client_uri\":\"https://client.example.org/\",\"logo_uri\":\"https://client.example.org/logo.svg\",\"policy_uri\":\"https://client/example.org/policy\"}"
        val metadata = OIDCClientMetadata.parse(JSONObjectUtils.parse(jsonMetadata))

// Make registration request
        val registrationRequest =
            OIDCClientRegistrationRequest(URI("$cozyBaseUrlString/auth/register"), metadata, null)

        val truc = registrationRequest.toHTTPRequest()

        truc.accept = "application/json"

        val regHTTPResponse = truc.send()

// Parse and check response
        val registrationResponse = OIDCClientRegistrationResponseParser.parse(regHTTPResponse)

        if (registrationResponse is ClientRegistrationErrorResponse) {
            val error = registrationResponse
                .errorObject
            return Result.Error(IOException("Error registering client : ${error.toJSONObject()}"))
        }

        val clientInformation = (registrationResponse as OIDCClientInformationResponse).oidcClientInformation

        return Result.Success(
            RegisteredOAuthClient(
                clientId = clientInformation.id.value,
                clientSecret = clientInformation.secret.value,
                registrationAccessToken = clientInformation.registrationAccessToken.value
            )
        )
    }

    fun unregister(cozyBaseUrlString:String,
                   clientId: String,
        masterAccessToken: String): Result<Boolean> {


        val req = ClientDeleteRequest(URI("$cozyBaseUrlString/auth/register/$clientId"),
            BearerAccessToken(masterAccessToken)
        )

        val deleteReponse = req.toHTTPRequest().send()

        if (! deleteReponse.indicatesSuccess()) {
            // We have an error
            val error = ClientRegistrationErrorResponse.parse(deleteReponse)
                .errorObject
            return Result.Error(IOException("Error registering client : ${error.toJSONObject()}"))
        }

        return Result.Success(true)
    }

    fun update(){

    }

    fun info(){

    }
}

