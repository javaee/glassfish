#
# The contents of this file are subject to the terms
# of the Common Development and Distribution License
# (the License).  You may not use this file except in
# compliance with the License.
#
# You can obtain a copy of the license at
# https://glassfish.dev.java.net/public/CDDLv1.0.html or
# glassfish/bootstrap/legal/CDDLv1.0.txt.
# See the License for the specific language governing
# permissions and limitations under the License.
#
# When distributing Covered Code, include this CDDL
# Header Notice in each file and include the License file
# at glassfish/bootstrap/legal/CDDLv1.0.txt.
# If applicable, add the following below the CDDL Header,
# with the fields enclosed by brackets [] replaced by
# you own identifying information:
# "Portions Copyrighted [year] [name of copyright owner]"
#
# Copyright 2006 Sun Microsystems, Inc. All rights reserved.
#
require 'cgi/force_nph'
require 'jdk_logger'

info    = $req.getRequestProcessor()

request_uri = $req.requestURI.to_s
headers = $req.getMimeHeaders()

# RFC3875 The Common Gateway Interface (CGI) Version 1.1
#ENV['AUTH_TYPE'] = $req.getAuthType().to_s
ENV['CONTENT_LENGTH'] = info.getContentLength().to_s
ENV['CONTENT_TYPE'] = $req.getContentType()

ENV['GATEWAY_INTERFACE'] = 'CGI/1.1'
ENV['PATH_INFO'] = request_uri
ENV['PATH_TRANSLATED'] = request_uri.split('?', 2).first
ENV['QUERY_STRING'] = $req.queryString().to_s
ENV['REMOTE_ADDR'] = info.getRemoteAddr()
#ENV['REMOTE_HOST'] = $req.remoteHost().to_s
#ENV['REMOTE_USER'] = $req.getRemoteUser().to_s

ENV['REQUEST_METHOD'] = info.getMethod()
ENV['SCRIPT_NAME'] = ''

ENV['SERVER_NAME'] = $req.serverName().to_s
ENV['SERVER_PORT'] = $req.getServerPort().to_s
ENV['SERVER_PROTOCOL'] = $req.protocol().to_s
ENV['SERVER_SOFTWARE'] = 'Grizzly/1.0.4'
ENV['REQUEST_URI'] = request_uri

for i in 0 ... headers.size
  name = headers.getName(i).to_s
  value = headers.getValue(i).to_s
  ENV['HTTP_' + name.upcase.tr('-','_')] = value
end

ActionController::AbstractRequest.relative_url_root = $root
require "dispatcher"

Dispatcher.dispatch
