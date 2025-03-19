'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { useParams, useSearchParams } from 'next/navigation';

interface Commit {
    sha: string;
    authorName: string;
    commitDate: string;
    message: string;
}

export default function CommitHistory() {
    const params = useParams();
    const searchParams = useSearchParams();
    const { owner, repo } = params;

    const page = parseInt(searchParams.get('page') || '1');
    const perPage = parseInt(searchParams.get('perPage') || '10');

    const [commits, setCommits] = useState<Commit[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        async function fetchCommits() {
            try {
                const response = await fetch(
                    `http://localhost:8080/api/repos/${owner}/${repo}/commits?page=${page}&perPage=${perPage}`,
                    { credentials: 'include' }
                );

                if (!response.ok) {
                    throw new Error('Failed to fetch commits');
                }

                const data = await response.json();
                setCommits(data.commits || []);
            } catch (error) {
                console.error('Error:', error);
                setError('Failed to load commits. Please try again later.');
            } finally {
                setLoading(false);
            }
        }

        fetchCommits();
    }, [owner, repo, page, perPage]);

    if (loading) {
        return (
            <div className="max-w-6xl mx-auto p-8">
                <div className="h-8 w-64 bg-gray-200 dark:bg-gray-700 rounded mb-8"></div>
                <div className="animate-pulse space-y-2">
                    <div className="h-10 bg-gray-200 dark:bg-gray-700 rounded w-full"></div>
                    {[1, 2, 3, 4, 5].map(i => (
                        <div key={i} className="h-16 bg-gray-200 dark:bg-gray-700 rounded w-full"></div>
                    ))}
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="max-w-6xl mx-auto p-8">
                <div className="bg-red-50 dark:bg-red-900/20 border-l-4 border-red-500 p-4 rounded">
                    <div className="flex">
                        <div className="flex-shrink-0">
                            <svg className="h-5 w-5 text-red-500" viewBox="0 0 20 20" fill="currentColor">
                                <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
                            </svg>
                        </div>
                        <div className="ml-3">
                            <p className="text-sm text-red-700 dark:text-red-400">{error}</p>
                        </div>
                    </div>
                </div>
                <Link href={`/repos`} className="mt-6 inline-flex items-center text-blue-600 dark:text-blue-400 hover:underline font-medium">
                    <svg className="w-5 h-5 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 19l-7-7m0 0l7-7m-7 7h18" />
                    </svg>
                    Back to Repositories
                </Link>
            </div>
        );
    }

    return (
        <div className="max-w-6xl mx-auto p-8">
            <div className="flex items-center justify-between mb-6">
                <h1 className="text-2xl md:text-3xl font-bold">
                    <span className="font-mono text-gray-500 dark:text-gray-400">{owner}/</span>
                    <span className="text-gray-900 dark:text-gray-100">{repo}</span>
                    <span className="ml-2 text-lg text-gray-500 dark:text-gray-400 font-normal">커밋 히스토리</span>
                </h1>

                <Link href="/repos" className="text-sm text-blue-600 dark:text-blue-400 hover:underline">
                    ← 저장소 목록
                </Link>
            </div>

            <div className="bg-white dark:bg-gray-800 shadow-md rounded-xl overflow-hidden mb-6">
                <div className="overflow-x-auto">
                    <table className="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
                        <thead className="bg-gray-50 dark:bg-gray-700/50">
                        <tr>
                            <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">커밋</th>
                            <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">작성자</th>
                            <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">날짜</th>
                            <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">메시지</th>
                        </tr>
                        </thead>
                        <tbody className="bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-700">
                        {commits.map((commitItem) => (
                            <tr key={commitItem.sha} className="hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors">
                                <td className="px-6 py-4 whitespace-nowrap text-sm font-mono">
                                    <Link href={`/repos/${owner}/${repo}/commits/${commitItem.sha}`} className="text-blue-600 dark:text-blue-400 hover:underline">
                                        {commitItem.sha.substring(0, 7)}
                                    </Link>
                                </td>
                                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-700 dark:text-gray-300">
                                    {commitItem.authorName}
                                </td>
                                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">
                                    {new Date(commitItem.commitDate).toLocaleString()}
                                </td>
                                <td className="px-6 py-4 text-sm text-gray-700 dark:text-gray-300 truncate max-w-xs">
                                    {commitItem.message}
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>
            </div>

            <div className="mt-6 flex justify-between items-center">
                <div className="text-sm text-gray-500 dark:text-gray-400">
                    {commits.length > 0 ?
                        `Showing ${(page - 1) * perPage + 1} to ${Math.min(page * perPage, (page - 1) * perPage + commits.length)} commits` :
                        'No commits found'}
                </div>
                <div className="flex space-x-2">
                    {page > 1 && (
                        <Link
                            href={`/repos/${owner}/${repo}/commits?page=${page - 1}&perPage=${perPage}`}
                            className="inline-flex items-center px-4 py-2 bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-600 rounded-md text-sm font-medium text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors"
                        >
                            이전
                        </Link>
                    )}
                    <Link
                        href={`/repos/${owner}/${repo}/commits?page=${page + 1}&perPage=${perPage}`}
                        className="inline-flex items-center px-4 py-2 bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-600 rounded-md text-sm font-medium text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors"
                    >
                        다음
                    </Link>
                </div>
            </div>
        </div>
    );
}
