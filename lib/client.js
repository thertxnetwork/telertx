import { Client } from 'tglib';
import * as readline from 'readline';
import * as path from 'path';
import * as os from 'os';
import * as fs from 'fs';

export class TelegramClient {
  constructor() {
    // API credentials - MUST be set via environment variables
    // Get them from https://my.telegram.org
    // DO NOT commit real credentials to version control
    this.apiId = process.env.TELEGRAM_API_ID;
    this.apiHash = process.env.TELEGRAM_API_HASH;
    
    if (!this.apiId || !this.apiHash) {
      throw new Error(
        'Missing Telegram API credentials. Please set TELEGRAM_API_ID and TELEGRAM_API_HASH environment variables.\n' +
        'Get your credentials from https://my.telegram.org'
      );
    }
    
    // TDLib data directory
    const dataDir = path.join(os.homedir(), '.telertx');
    if (!fs.existsSync(dataDir)) {
      fs.mkdirSync(dataDir, { recursive: true });
    }
    
    // Initialize TDLib client
    this.client = new Client({
      apiId: this.apiId,
      apiHash: this.apiHash,
      databaseDirectory: path.join(dataDir, 'db'),
      filesDirectory: path.join(dataDir, 'files'),
      databaseEncryptionKey: '',
      verbosityLevel: 2,
      useTestDc: false,
      useChatInfoDatabase: true,
      useMessageDatabase: true,
      useSecretChats: false,
    });
    
    this.rl = null;
    this.setupCallbacks();
  }
  
  setupCallbacks() {
    // Handle updates
    this.client.registerCallback('td:update', (update) => {
      // console.log('[Update]', update['@type']);
    });
    
    // Handle errors
    this.client.registerCallback('td:error', (error) => {
      console.error('[Error]', error);
    });
    
    // Handle input requests
    const defaultHandler = this.client.callbacks['td:getInput'];
    this.client.registerCallback('td:getInput', async (args) => {
      return await this.handleInput(args, defaultHandler);
    });
  }
  
  async handleInput(args, defaultHandler) {
    const { string, extras = {} } = args;
    
    // Create readline interface if not exists
    if (!this.rl) {
      this.rl = readline.createInterface({
        input: process.stdin,
        output: process.stdout
      });
    }
    
    return new Promise((resolve) => {
      let prompt = '';
      
      switch (string) {
        case 'tglib.input.AuthorizationType':
          resolve('user');
          return;
          
        case 'tglib.input.AuthorizationValue':
          prompt = 'Enter your phone number (with country code, e.g., +1234567890): ';
          break;
          
        case 'tglib.input.FirstName':
          prompt = 'Enter your first name (for new account): ';
          break;
          
        case 'tglib.input.AuthorizationCode':
          prompt = 'Enter the verification code you received: ';
          break;
          
        case 'tglib.input.AuthorizationCodeIncorrect':
          prompt = 'Incorrect code. Please enter the verification code again: ';
          break;
          
        case 'tglib.input.AuthorizationPassword':
          prompt = `Enter your 2FA password${extras.hint ? ` (hint: ${extras.hint})` : ''}: `;
          break;
          
        case 'tglib.input.AuthorizationPasswordIncorrect':
          prompt = `Incorrect password. Please enter your 2FA password again${extras.hint ? ` (hint: ${extras.hint})` : ''}: `;
          break;
          
        default:
          // Use default handler for unknown inputs
          return defaultHandler(args).then(resolve);
      }
      
      this.rl.question(prompt, (answer) => {
        resolve(answer.trim());
      });
    });
  }
  
  async login() {
    try {
      // Wait for the client to be ready (authentication complete)
      await this.client.ready;
      
      // Close readline interface
      if (this.rl) {
        this.rl.close();
        this.rl = null;
      }
      
      return true;
    } catch (error) {
      if (this.rl) {
        this.rl.close();
        this.rl = null;
      }
      throw error;
    }
  }
  
  async logout() {
    try {
      // Log out from Telegram
      await this.client._send({
        '@type': 'logOut'
      });
      
      return true;
    } catch (error) {
      throw error;
    }
  }
  
  async getStatus() {
    try {
      // Get authorization state
      const authState = await this.client._send({
        '@type': 'getAuthorizationState'
      });
      
      const isLoggedIn = authState['@type'] === 'authorizationStateReady';
      
      let user = null;
      if (isLoggedIn) {
        // Get current user info
        user = await this.client._send({
          '@type': 'getMe'
        });
      }
      
      return {
        isLoggedIn,
        user,
        authState: authState['@type']
      };
    } catch (error) {
      throw error;
    }
  }
  
  async close() {
    if (this.rl) {
      this.rl.close();
      this.rl = null;
    }
    
    try {
      await this.client._destroy();
    } catch (error) {
      // Ignore errors during cleanup
    }
  }
}
