///
/// Copyright © 2016-2024 The Sobeam Authors
///
/// Licensed under the Apache License, Version 2.0 (the "License");
/// you may not use this file except in compliance with the License.
/// You may obtain a copy of the License at
///
///     http://www.apache.org/licenses/LICENSE-2.0
///
/// Unless required by applicable law or agreed to in writing, software
/// distributed under the License is distributed on an "AS IS" BASIS,
/// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
/// See the License for the specific language governing permissions and
/// limitations under the License.
///

import express from 'express';
import config from 'config';
import { _logger}  from './config/logger';
import path from 'path';
import http, { ServerResponse } from 'http';
import httpProxy from 'http-proxy';
import compression from 'compression';
import historyApiFallback from 'connect-history-api-fallback';
import { Socket } from 'net';
import { RequestHandler } from 'serve-static';

const logger = _logger('main');

let server: http.Server | null;
let connections: Socket[] = [];

(async() => {
    try {
        logger.info('Starting SoBeam Web UI Microservice...');

        const bindAddress: string = config.get('server.address');
        const bindPort = Number(config.get('server.port'));

        const sobeamEnableProxy: string = config.get('sobeam.enableProxy');

        const sobeamHost: string = config.get('sobeam.host');
        const sobeamPort = Number(config.get('sobeam.port'));

        logger.info('Bind address: %s', bindAddress);
        logger.info('Bind port: %s', bindPort);
        logger.info('SoBeam Enable Proxy: %s', sobeamEnableProxy);
        logger.info('SoBeam host: %s', sobeamHost);
        logger.info('SoBeam port: %s', sobeamPort);

        const useApiProxy = sobeamEnableProxy === "true";

        let webDir = path.join(__dirname, 'web');

        if (typeof process.env.WEB_FOLDER !== 'undefined') {
            webDir = path.resolve(process.env.WEB_FOLDER);
        }
        logger.info('Web folder: %s', webDir);

        const app = express();
        server = http.createServer(app);

        let apiProxy: httpProxy;
        if (useApiProxy) {
            apiProxy = httpProxy.createProxyServer({
                target: {
                    host: sobeamHost,
                    port: sobeamPort
                }
            });

            apiProxy.on('error', (err, req, res) => {
                logger.warn('API proxy error: %s', err.message);
                if (res instanceof ServerResponse) {
                    res.writeHead(500);
                    const error = err as any;
                    if (error.code && error.code === 'ECONNREFUSED') {
                        res.end('Unable to connect to SoBeam server.');
                    } else {
                        res.end('SoBeam server connection error: ' + error.code ? error.code : '');
                    }
                }
            });
            app.all('/api/*', (req, res) => {
              logger.debug(req.method + ' ' + req.originalUrl);
              apiProxy.web(req, res);
            });

            app.all('/static/rulenode/*', (req, res) => {
              apiProxy.web(req, res);
            });

            server.on('upgrade', (req, socket, head) => {
              apiProxy.ws(req, socket, head);
            });
        }

        app.use(historyApiFallback());
        app.use(compression());

        const root = path.join(webDir, 'public');

        const staticServe = express.static(root);

        const indexHtmlFallback: RequestHandler<any> = (req, res, next) => {
          res.sendFile(path.join(root, 'index.html'));
        };

        app.use(staticServe);

        const indexHtmlFallbackPaths = [
          /^\/resources\/scada-symbols\/(?:system|tenant)\/[^/]+$/
        ];

        app.use(indexHtmlFallbackPaths, indexHtmlFallback);

        server.listen(bindPort, bindAddress, () => {
            logger.info('==> 🌎  Listening on port %s.', bindPort);
            logger.info('Started SoBeam Web UI Microservice.');
        }).on('error', async (error) => {
            logger.error('Failed to start SoBeam Web UI Microservice: %s', error.message);
            logger.error(error.stack);
            await exit(-1);
        });

        server.on('connection', connection => {
            connections.push(connection);
            connection.on('close', () => connections = connections.filter(curr => curr !== connection));
        });

    } catch (e: any) {
        logger.error('Failed to start SoBeam Web UI Microservice: %s', e.message);
        logger.error(e.stack);
        await exit(-1);
    }
})();

[`SIGINT`, `SIGUSR1`, `SIGUSR2`, `uncaughtException`, `SIGTERM`].forEach((eventType) => {
    process.once(eventType, async () => {
        logger.info(`${eventType} signal received`);
        await exit(0);
    });
})

process.on('exit', async (code: number) => {
    logger.info(`SoBeam Web UI Microservice has been stopped. Exit code: ${code}.`);
});

async function exit(status: number) {
    logger.info('Exiting with status: %d ...', status);
    if (server) {
        logger.info('Stopping HTTP Server...');
        connections.forEach(curr => curr.end(() => curr.destroy()));
        const _server = server;
        server = null;
        const serverClosePromise = new Promise<void>(
            (resolve, reject) => {
                _server.close((err) => {
                    logger.info('HTTP Server stopped.');
                    resolve();
                });
            }
        );
        await serverClosePromise;
    }
    process.exit(status);
}
