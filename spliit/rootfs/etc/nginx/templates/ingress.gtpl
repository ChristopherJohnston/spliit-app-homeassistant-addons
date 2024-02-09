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
        proxy_redirect '/' $http_x_ingress_path/;

        # Disable compression or the sub_filters will not be applied
        proxy_set_header Accept-Encoding "";

        # add the ingress path to all links
        sub_filter_types *;
        sub_filter 'href="/' 'href="$http_x_ingress_path/';
        sub_filter 'src="/' 'src="$http_x_ingress_path/';

        # Sub ingress path to the image srcset tags
        sub_filter 'srcset="/' 'srcset="$http_x_ingress_path/';
        sub_filter ', /_next/image' ', $http_x_ingress_path/_next/image';

        sub_filter_once off;
    }
}