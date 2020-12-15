/*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package fr.insee.sugoi.old.services.decider;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("OldAuthorizeMethodDecider")
public class AuthorizeMethodDecider {

    @Value("${fr.insee.sugoi.api.old.regexp.role.consultant:}")
    private String regexpConsult;

    @Value("${fr.insee.sugoi.api.old.regexp.role.gestionnaire:}")
    private String regexpGest;

    @Value("${fr.insee.sugoi.api.old.regexp.role.admin:}")
    private String regexpAdmin;

    @Value("${fr.insee.sugoi.api.old.enable.preauthorize:false}")
    private boolean enable;

    public boolean isAtLeastConsultant(String domaine) {
        System.out.println(enable);
        if (enable) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            return authentication.getAuthorities().stream()
                    .map(authority -> extractRole(authority.getAuthority(), regexpConsult))
                    .filter(authority -> authority != null).collect(Collectors.toList()).size() > 0
                    || isAtLeastGestionnaire(domaine) || isAdmin();
        }
        return true;
    }

    public boolean isAtLeastGestionnaire(String domaine) {
        if (enable) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            return authentication.getAuthorities().stream()
                    .map(authority -> extractRole(authority.getAuthority(), regexpGest))
                    .filter(authority -> authority != null).collect(Collectors.toList()).size() > 0 || isAdmin();
        }
        return true;
    }

    public boolean isAdmin() {
        if (enable) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            return authentication.getAuthorities().stream()
                    .map(authority -> extractRole(authority.getAuthority(), regexpAdmin))
                    .filter(authority -> authority != null).collect(Collectors.toList()).size() > 0;
        }
        return true;
    }

    private String extractRole(String authority, String regexp) {
        Pattern pattern = Pattern.compile(regexp);
        Matcher matcher = pattern.matcher(authority);
        if (matcher.matches()) {
            return authority;
        }
        return null;
    }
}
