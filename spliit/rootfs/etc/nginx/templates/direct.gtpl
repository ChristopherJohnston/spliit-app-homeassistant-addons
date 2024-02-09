server {
    {{ if not .ssl }}
    listen 80 default_server;
    {{ else }}
    listen 80 default_server ssl http2;
    {{ end }}

    include /etc/nginx/includes/server_params.conf;

    {{ if .ssl }}
    include /etc/nginx/includes/ssl_params.conf;

    ssl_certificate /ssl/{{ .certfile }};
    ssl_certificate_key /ssl/{{ .keyfile }};
    {{ end }}

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
        root /var/www/spliit/build/server/pages/;
        try_files $uri $uri.html /index.html @nextserver;
        add_header X-Custom-HeaderNextServer "hitting /";
    }

    location ~ /groups {
        root /var/www/spliit/build/server/pages;
        try_files $uri $uri.html @nextserver;
        add_header X-Custom-HeaderNextServer "hitting /groups";
    }

    location @nextserver {
        absolute_redirect off;

        proxy_pass http://{{.interface}}:3000;
        add_header X-Custom-HeaderNextServer "Value for Custom Header @nextserver";

        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header Origin "";
    }
}