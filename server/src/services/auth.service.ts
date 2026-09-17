import { v4 as uuidv4 } from 'uuid';
import { userRepository } from '../repositories/user.repository.js';
import { hashPassword, comparePassword } from '../utils/password.js';
import { signToken } from '../utils/jwt.js';
import { ConflictError, UnauthorizedError, NotFoundError, BadRequestError } from '../utils/errors.js';

export class AuthService {
  async signup(data: {
    handle: string;
    email?: string;
    passwordPin?: string;
    password?: string;
    displayName?: string;
    bio?: string;
  }) {
    let handle = data.handle.trim();
    if (!handle.startsWith('@')) {
      handle = `@${handle}`;
    }

    // Check existing handle
    const existingByHandle = await userRepository.findByHandle(handle);
    if (existingByHandle) {
      throw new ConflictError('Username/handle is already taken');
    }

    // Check existing email
    if (data.email) {
      const existingByEmail = await userRepository.findByEmail(data.email);
      if (existingByEmail) {
        throw new ConflictError('Email is already registered');
      }
    }

    const rawPass = data.password || data.passwordPin || '1234';
    const passwordHash = await hashPassword(rawPass);
    const userId = `user_${uuidv4().replace(/-/g, '').slice(0, 12)}`;

    const user = await userRepository.createUser({
      id: userId,
      handle,
      email: data.email || null,
      passwordHash,
      role: 'USER',
      isVerified: false,
    });

    const displayName = data.displayName?.trim() || handle.replace('@', '');
    const profile = await userRepository.createProfile({
      userId,
      displayName,
      handle,
      bio: data.bio || '',
      avatarUrl: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=300&q=80',
    });

    const token = signToken({
      userId: user.id,
      handle: user.handle,
      role: user.role,
    });

    return {
      user: {
        id: user.id,
        handle: user.handle,
        email: user.email,
        displayName: profile.display_name,
        avatarUrl: profile.avatar_url,
        bio: profile.bio,
        role: user.role,
        isVerified: user.is_verified,
      },
      token,
    };
  }

  async login(data: {
    handle?: string;
    handleOrEmail?: string;
    password?: string;
    passwordPin?: string;
  }) {
    const identifier = (data.handleOrEmail || data.handle || '').trim();
    const rawPass = data.password || data.passwordPin;

    if (!identifier || !rawPass) {
      throw new BadRequestError('Credentials required');
    }

    const user = await userRepository.findByHandleOrEmail(identifier);
    if (!user) {
      throw new UnauthorizedError('Invalid credentials');
    }

    if (!user.is_active) {
      throw new UnauthorizedError('Account is disabled');
    }

    const isMatch = await comparePassword(rawPass, user.password_hash);
    if (!isMatch) {
      throw new UnauthorizedError('Invalid credentials');
    }

    const profile = await userRepository.getProfile(user.id, user.id);

    const token = signToken({
      userId: user.id,
      handle: user.handle,
      role: user.role,
    });

    return {
      user: {
        id: user.id,
        handle: user.handle,
        email: user.email,
        displayName: profile?.displayName || user.handle.replace('@', ''),
        avatarUrl: profile?.avatarUrl || '',
        bio: profile?.bio || '',
        role: user.role,
        isVerified: user.is_verified,
        followingCount: profile?.followingCount || 0,
        followersCount: profile?.followersCount || 0,
        likesCount: profile?.likesCount || 0,
        videosCount: profile?.videosCount || 0,
      },
      token,
    };
  }

  async changePin(userId: string, oldPin: string, newPin: string) {
    const user = await userRepository.findById(userId);
    if (!user) {
      throw new NotFoundError('User not found');
    }

    const isMatch = await comparePassword(oldPin, user.password_hash);
    if (!isMatch) {
      throw new UnauthorizedError('Current PIN does not match');
    }

    const newHash = await hashPassword(newPin);
    await userRepository.updatePassword(userId, newHash);

    return { success: true, message: 'PIN updated successfully' };
  }

  async getCurrentUser(userId: string) {
    const user = await userRepository.findById(userId);
    if (!user) {
      throw new NotFoundError('User not found');
    }

    const profile = await userRepository.getProfile(user.id, user.id);

    return {
      id: user.id,
      handle: user.handle,
      email: user.email,
      displayName: profile?.displayName || user.handle.replace('@', ''),
      avatarUrl: profile?.avatarUrl || '',
      bio: profile?.bio || '',
      role: user.role,
      isVerified: user.is_verified,
      isPrivate: profile?.isPrivate || false,
      followingCount: profile?.followingCount || 0,
      followersCount: profile?.followersCount || 0,
      likesCount: profile?.likesCount || 0,
      videosCount: profile?.videosCount || 0,
    };
  }
}

export const authService = new AuthService();
