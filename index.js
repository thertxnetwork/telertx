#!/usr/bin/env node

import { Command } from 'commander';
import chalk from 'chalk';
import { TelegramClient } from './lib/client.js';
import * as readline from 'readline';

const program = new Command();

program
  .name('telertx')
  .description('Simple Telegram CLI client with login/logout functionality')
  .version('1.0.0');

program
  .command('login')
  .description('Login to Telegram')
  .action(async () => {
    console.log(chalk.blue('🔐 TeleRTX - Telegram Login\n'));
    
    const client = new TelegramClient();
    
    try {
      await client.login();
      console.log(chalk.green('\n✓ Login successful!'));
      await client.close();
      process.exit(0);
    } catch (error) {
      console.error(chalk.red('\n✗ Login failed:'), error.message);
      await client.close();
      process.exit(1);
    }
  });

program
  .command('logout')
  .description('Logout from Telegram')
  .action(async () => {
    console.log(chalk.blue('🚪 TeleRTX - Telegram Logout\n'));
    
    const rl = readline.createInterface({
      input: process.stdin,
      output: process.stdout
    });
    
    rl.question('Are you sure you want to logout? (yes/no): ', async (answer) => {
      rl.close();
      
      if (answer.toLowerCase() !== 'yes' && answer.toLowerCase() !== 'y') {
        console.log(chalk.yellow('Logout cancelled.'));
        process.exit(0);
      }
      
      const client = new TelegramClient();
      
      try {
        await client.logout();
        console.log(chalk.green('\n✓ Logout successful!'));
        await client.close();
        process.exit(0);
      } catch (error) {
        console.error(chalk.red('\n✗ Logout failed:'), error.message);
        await client.close();
        process.exit(1);
      }
    });
  });

program
  .command('status')
  .description('Check login status')
  .action(async () => {
    console.log(chalk.blue('📊 TeleRTX - Status Check\n'));
    
    const client = new TelegramClient();
    
    try {
      const status = await client.getStatus();
      
      if (status.isLoggedIn) {
        console.log(chalk.green('✓ Status: Logged in'));
        if (status.user) {
          console.log(chalk.white(`   User: ${status.user.first_name}${status.user.last_name ? ' ' + status.user.last_name : ''}`));
          console.log(chalk.white(`   Phone: ${status.user.phone_number}`));
          console.log(chalk.white(`   Username: @${status.user.username || 'N/A'}`));
        }
      } else {
        console.log(chalk.yellow('⚠ Status: Not logged in'));
        console.log(chalk.white('   Run "telertx login" to login'));
      }
      
      await client.close();
      process.exit(0);
    } catch (error) {
      console.error(chalk.red('\n✗ Status check failed:'), error.message);
      await client.close();
      process.exit(1);
    }
  });

program
  .action(() => {
    // Default action - show help
    program.help();
  });

program.parse();
