# Validate the X-Ingress-Path header against the known HA Ingress URL pattern
# before embedding it in JavaScript. Any value that does not match is replaced
# with an empty string so the client-side patch script is a no-op.
map $http_x_ingress_path $safe_ingress_path {
    ~^/api/hassio_ingress/[a-zA-Z0-9][a-zA-Z0-9_-]*$  $http_x_ingress_path;
    default                                  "";
}

server {
    listen {{ .interface }}:8099 default_server;

    include /etc/nginx/includes/server_params.conf;

    allow   172.30.32.2;
    deny    all;

    proxy_cache off;
    proxy_http_version 1.1;
    proxy_set_header Upgrade $http_upgrade;
    proxy_set_header Connection "upgrade";
    proxy_cache_bypass $http_upgrade;

    # You will observe hits to _next/data and _next/image going thorugh @nextserver instead of /var/www/spliit/build,
    # you will not find these in next server file system, instead next server smartly manages these(faster transition things and image optimisation things) in
    # it's server temporary memory(say like RAM). Hit will always go to next server only.
    location ^~ /_next {
        alias /var/www/spliit/build;
        try_files $uri @nextserver;
        expires 365d;
        add_header Cache-Control 'public';
        add_header X-Custom-Header_next "hitting _NEXT";
    }

    location / {
        try_files $uri $uri.html /index.html @nextserver;
        add_header X-Custom-HeaderNextServer "hitting /";
    }

    location @nextserver {
        absolute_redirect off;

        proxy_pass http://{{.interface}}:3000;
        add_header X-Custom-HeaderNextServer "Hitting @nextserver";

        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header Origin "";
        proxy_redirect '/' $safe_ingress_path/;

        # Disable compression so sub_filter works on the response body
        proxy_set_header Accept-Encoding "";

        # Only apply sub_filter to HTML responses to avoid corrupting
        # JSON, RSC payloads, and other non-HTML content types
        sub_filter_types text/html;
        sub_filter_once off;

        # Rewrite absolute paths in HTML attributes to include the ingress base path
        sub_filter 'href="/' 'href="$safe_ingress_path/';
        sub_filter 'src="/' 'src="$safe_ingress_path/';
        sub_filter 'action="/' 'action="$safe_ingress_path/';

        # Sub ingress path to the image srcset tags
        sub_filter 'srcset="/' 'srcset="$safe_ingress_path/';
        sub_filter ', /_next/image' ', $safe_ingress_path/_next/image';

        # Inject a script as early as possible in <head> that patches
        # window.fetch, pushState, and replaceState so that Next.js
        # client-side navigation and RSC data fetches are sent through
        # the correct HA Ingress URL rather than the bare domain root.
        #
        # The injected logic (expanded for readability):
        #   var p = "<ingress-path>";          // e.g. /api/hassio_ingress/TOKEN
        #   if (!p) return;                    // no-op when accessed directly
        #   // Patch fetch so RSC / API requests carry the ingress prefix.
        #   // Handles both string URLs and Request objects; skips protocol-
        #   // relative URLs (starting with "//") and already-prefixed URLs.
        #   var _f = window.fetch;
        #   window.fetch = function(u, o) {
        #     if (typeof u === "string" && u[0]==="/" && u.slice(0,2)!="//"
        #         && u.slice(0,p.length) !== p) {
        #       u = p + u;
        #     } else if (typeof Request!=="undefined" && u instanceof Request) {
        #       try {
        #         var pu = new URL(u.url);
        #         if (pu.origin===location.origin && pu.pathname[0]==="/"
        #             && pu.pathname.slice(0,p.length)!==p) {
        #           pu.pathname = p + pu.pathname;
        #           u = new Request(pu.href, u);
        #         }
        #       } catch(e) {}
        #     }
        #     return _f.call(this, u, o);
        #   };
        #   // Patch history so the address bar stays on the ingress URL
        #   ["pushState","replaceState"].forEach(function(m) {
        #     var h = window.history, orig = h[m].bind(h);
        #     h[m] = function(s, t, u) {
        #       if (u && typeof u==="string" && u[0]==="/" && u.slice(0,2)!="//"
        #           && u.slice(0,p.length)!==p) { u = p + u; }
        #       return orig(s, t, u);
        #     };
        #   });
        sub_filter '<head>' '<head><script>(function(){var p="$safe_ingress_path";if(!p)return;var _f=window.fetch;window.fetch=function(u,o){if(typeof u==="string"&&u[0]==="/"&&u.slice(0,2)!="//"&&u.slice(0,p.length)!==p){u=p+u;}else if(typeof Request!=="undefined"&&u instanceof Request){try{var pu=new URL(u.url);if(pu.origin===location.origin&&pu.pathname[0]==="/"&&pu.pathname.slice(0,p.length)!==p){pu.pathname=p+pu.pathname;u=new Request(pu.href,u);}}catch(e){}}return _f.call(this,u,o);};["pushState","replaceState"].forEach(function(m){var h=window.history,orig=h[m].bind(h);h[m]=function(s,t,u){if(u&&typeof u==="string"&&u[0]==="/"&&u.slice(0,2)!="//"&&u.slice(0,p.length)!==p){u=p+u;}return orig(s,t,u);};});})();</script>';
    }
}