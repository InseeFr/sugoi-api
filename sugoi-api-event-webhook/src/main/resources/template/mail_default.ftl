{
    "MessageTemplate": 
        {
            "Sender": "noreply@insee.fr",
            "Subject": "Votre nouveau mot de passe",
            "Content": "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\"><html><body><p>Bonjour,</p><p>Suite à votre demande, voici votre nouveau mot de passe <#if properties.application??> pour l'accès à ${properties.application} </#if>:</p><p>${password}</p><#if properties.assistMail??><p>Pour toute demande d'assistance, vous pouvez contacter ${properties.assistMail} par courrier électronique.</p></#if><p>Cordialement,</p><#if properties.signature??><p>${properties.signature}<#else>Assistance Insee</#if></p></body></html>"
        },
    "Recipients": 
        { 
            "Recipient": 
                [{ "Address": "${mail}" }] 
        }
}