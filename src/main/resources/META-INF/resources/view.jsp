<%@ include file="/init.jsp" %>
<%
boolean isRegister = (Boolean) request.getAttribute("isRegister");
%>

<liferay-portlet:resourceURL var="getEmailUrl">
	<liferay-portlet:param  name="cmd" value="getEmail"/>
</liferay-portlet:resourceURL>

<liferay-portlet:resourceURL var="getTokenUrl">
	<liferay-portlet:param  name="cmd" value="getToken"/>
</liferay-portlet:resourceURL>

<h1>Dentro de retorno</h1>

<script>
const isUserValidReturn = <%= isRegister%>;

if(isUserValidReturn){
	var returnUri = "google";
	var redirectUrl;
	var clientId="growjet";
	var keycloakUrl;
	var userDataFromIdToken = null;
	var errorMessage = null;
	var accessToken = null;
	var ckeckIfAccessTokenExists;
	var keycloakLogout;
	var getUserInfo;
	
	$( document ).ready(function() {		
		$(window).on('beforeunload', function(){
			console.log("Revisando localstorage");
			localStorage.removeItem("access_token");
		});
		
		var getIdToken = function () {
			if(userDataFromidToken){
				document.getElementById("h-text").innerText = JSON.stringify(userDataFromIdToken, null, 2);
			}
		}
		
		var displayUserInfoIfTokenExists = function () {
			console.log("validando si existe el token");
			if(localStorage.getItem("access_token") || sessionStorage.getItem("access_token")){
				document.getElementById("infoButton").style.display = "";
			}else if(errorMessage){
				document.getElementById("infoButton").innerText = errorMessage;
			}else{
				let clientSecretElement = "e609371f-3b7b-42ae-9086-bb3fbc4c5b1b";
				console.log(clientSecretElement);
				// validando el liente secreto
				if(clientSecretElement === 'undefined' || clientSecretElement === ""){
					console.log("El client secret esta vacio");
				}else{
					console.log('obteniendo los parametros de location');
					const queryString = window.location.search;
					const urlParams = new URLSearchParams(queryString);
					
					if(urlParams.has('error')){
						console.log('Validando que tenga error');
						const error = urlParams.get('error');
						const errorDescripcion = urlParams.get('error_description');
						const message = "Error: \"" + error + "\"\n\nError description: \"" + errorDescripcion + "\"";
						errorMessage = message;
						console.log(message);
						if(Liferay.ThemeDisplay.getLanguageId() === 'en_US'){
							getAlerta('Daon Message','Sorry the biometric registration has failed');
						}else{
							getAlerta('Daon Mensajes','Lo sentimos el registro biométrico ha fallado');
						}
					}else if(urlParams.has('code')){
						console.log("No hubo error");
						console.log(urlParams);
						let code = urlParams.get('code');
						console.log(code);
						const emailName = Liferay.ThemeDisplay.getUserEmailAddress(); 
						postResource('${getEmailUrl}', emailName);
						console.log(code);
						console.log(clientId);
						console.log(clientSecretElement);
						postToken('${getTokenUrl}', code, clientId, clientSecretElement, 'authorization_code', 'https://jetbank.growjet.com.mx/');
						if(Liferay.ThemeDisplay.getLanguageId() === 'en_US'){
							getAlerta('Daon Message',`<section class="container">
									<article class="row justify-content-md-center align-items-center"> 
									<article class="col-md-6">			
									Great! biometric registration has been completed successfully
										</article>
									<article class="col-md-6">
									<div class="d-flex justify-content-center align-items-center">
									<img src="https://jetbank.growjet.com.mx/documents/43918/0/good.png/fbfe2683-5205-2e54-02fb-374ed026d0f4?t=1655415481467">
									</div>
									</article>
								</article>		
					</section>`);
						}else{
							getAlerta('Daon Mensajes',`<section class="container">
									<article class="row justify-content-md-center align-items-center"> 
									<article class="col-md-6">			
									¡Genial! se ha completado el registro biométrico de manera exitosa
										</article>
									<article class="col-md-6">
									<div class="d-flex justify-content-center align-items-center">
									<img src="https://jetbank.growjet.com.mx/documents/43918/0/good.png/fbfe2683-5205-2e54-02fb-374ed026d0f4?t=1655415481467">
									</div>
									</article>
								</article>		
					</section>`);
						}
						fetch("https://emea-saas-dobs-idp.identityx-cloud.com/auth/realms/" + clientId + "/protocol/openid-connect/token",{
							body: new URLSearchParams({
								'code':code,
								'client_id':clientId,
								'client_secret':clientSecretElement,
								'grant_type': 'authorization_code',
								'redirect_uri': returnUri
							}),
							method: "post"
						}).then(response => response.json()).then(data => {
							var idToken = data["id_token"];
							accessToken = data["access_token"];
							localStorge.setItem("access_token", data ["access_token"]);
							var idSplit = idToken.split(".");
							var base64 = idSplit[1].replace(/-/g, '+').replace(/g/,'/');
							var payloadString = decodeURIComponent(atob(base64).split('').map(function (c){
								return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
							}).join(''));
							var payload = JSON.parse(payloadString);
							userDataFromIdToken = payload;
							
							document.getElementById("h-text").innerText = JSON.stringify(payload, null, 2);
							displayUserInfoIfTokenExists();
						})
						.catch((error) =>{
							let message = error + ". Client secret correct ?";
							document.getElementById("h-text").innerText = message;
							errorMessage = message;
						});
						
						clientSecretElement.value = "";
						clientSecretElement.style.border = "";
						document.getElementById("secret").style.display = "none";
					}else{
						console.log('Ya cuentas con onboarding');
					}
				}
			}
		}
		displayUserInfoIfTokenExists();
	});	
}
function getAlerta(titulo ,content){
	$.alert({
	    title: titulo,
	    content: content,
	    type: 'green',
	    columnClass: 'col-md-5',
	    typeAnimated: true,
	});
}

function postResource(url_portlet,emailName){
	$.ajax({
		url: url_portlet,
		type: 'post',
		data: {
			<portlet:namespace/>getEmail : emailName
			},
	}).done(function(data) {
		console.log('Fue exitoso el Post');
		console.log(data);
	}).fail(function(e) {
		console.log('Ocurrio un error en el post');
		console.log(e);
		console.info('Fallo en la seccion de regreso');
	});
}

function postToken(url_portlet, code, client_id, client_secret, grant_type, redirect_uri){
	$.ajax({
		url: url_portlet,
		type: 'post',
		data: {
			<portlet:namespace/>getCode : code,
			<portlet:namespace/>getClientId : client_id,
			<portlet:namespace/>getClientSecret : client_secret,
			<portlet:namespace/>getGrantType : grant_type,
			<portlet:namespace/>getRedirectUri : redirect_uri
			},
	}).done(function(data) {
		console.log('Fue exitoso el Post token');
		console.log(data);
	}).fail(function(e) {
		console.log('Ocurrio un error en el post');
		console.log(e);
		console.info('Fallo en la seccion de regreso');
	});
}

</script>