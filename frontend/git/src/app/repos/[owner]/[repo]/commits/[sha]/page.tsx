'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { useParams } from 'next/navigation';

export default function CommitDetail() {
    const params = useParams();
    const { owner, repo, sha } = params;

    const [commit, setCommit] = useState<any>(null);
    const [changedFiles, setChangedFiles] = useState<any[]>([]);
    const [selectedFile, setSelectedFile] = useState<number | null>(null);
    const [fileDiff, setFileDiff] = useState<{ oldContent: string, newContent: string } | null>(null);
    const [loading, setLoading] = useState(true);
    const [diffLoading, setDiffLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    // 통합 뷰 옵션 제거 - 항상 split 모드만 사용
    const viewMode = 'split';

    useEffect(() => {
        async function fetchCommitDetail() {
            try {
                const response = await fetch(
                    `http://localhost:8080/api/repos/${owner}/${repo}/commits/${sha}`,
                    { credentials: 'include' }
                );

                if (!response.ok) {
                    throw new Error('Failed to fetch commit details');
                }

                const data = await response.json();
                setCommit(data.commit);
                setChangedFiles(data.changedFiles || []);
                if (data.changedFiles && data.changedFiles.length > 0) {
                    setSelectedFile(0);
                }
            } catch (error) {
                console.error('Error:', error);
                setError('Failed to load commit details. Please try again later.');
            } finally {
                setLoading(false);
            }
        }

        fetchCommitDetail();
    }, [owner, repo, sha]);

    useEffect(() => {
        async function fetchFileDiff() {
            if (selectedFile === null || !changedFiles[selectedFile]) return;

            const filePath = changedFiles[selectedFile].fileName;
            setDiffLoading(true);

            try {
                const response = await fetch(
                    `http://localhost:8080/api/repos/${owner}/${repo}/commit/${sha}/file?filePath=${encodeURIComponent(filePath)}`,
                    { credentials: 'include' }
                );

                if (!response.ok) {
                    throw new Error('Failed to fetch file diff');
                }

                const data = await response.json();
                setFileDiff(data);
            } catch (error) {
                console.error('Error fetching file diff:', error);
                setFileDiff(null);
            } finally {
                setDiffLoading(false);
            }
        }

        fetchFileDiff();
    }, [owner, repo, sha, selectedFile, changedFiles]);

    if (loading) {
        return (
            <div className="max-w-6xl mx-auto p-8">
                <div className="h-8 w-64 bg-gray-200 dark:bg-gray-700 rounded mb-8"></div>
                <div className="animate-pulse space-y-4">
                    <div className="h-32 bg-gray-200 dark:bg-gray-700 rounded w-full"></div>
                    <div className="h-8 bg-gray-200 dark:bg-gray-700 rounded w-1/3"></div>
                    <div className="space-y-2">
                        {[1].map(i => (
                            <div key={i} className="h-24 bg-gray-200 dark:bg-gray-700 rounded w-full"></div>
                        ))}
                    </div>
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
                <Link href={`/repos/${owner}/${repo}/commits`} className="mt-6 inline-flex items-center text-blue-600 dark:text-blue-400 hover:underline font-medium">
                    <svg className="w-5 h-5 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 19l-7-7m0 0l7-7m-7 7h18" />
                    </svg>
                    Back to Commit History
                </Link>
            </div>
        );
    }

    return (
        <div className="max-w-6xl mx-auto p-8">
            <div className="flex items-center justify-between mb-6">
                <h1 className="text-2xl md:text-3xl font-bold">
                    <span className="font-mono text-gray-500 dark:text-gray-400">{owner}/{repo}</span>
                    <span className="ml-2 text-lg text-gray-500 dark:text-gray-400 font-normal">커밋 상세</span>
                </h1>

                <Link href={`/repos/${owner}/${repo}/commits`} className="text-sm text-blue-600 dark:text-blue-400 hover:underline">
                    ← 커밋 히스토리
                </Link>
            </div>

            <div className="bg-white dark:bg-gray-800 shadow-md rounded-xl overflow-hidden mb-6 p-6">
                <h2 className="text-xl font-semibold mb-4 text-gray-900 dark:text-gray-100">{commit.message}</h2>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
                    <div className="flex items-center text-sm text-gray-600 dark:text-gray-400">
                        <svg className="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                        </svg>
                        <span className="font-medium mr-1">Author:</span> {commit.authorName} &lt;{commit.authorEmail}&gt;
                    </div>
                    <div className="flex items-center text-sm text-gray-600 dark:text-gray-400">
                        <svg className="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                        </svg>
                        <span className="font-medium mr-1">Date:</span> {new Date(commit.commitDate).toLocaleString()}
                    </div>
                </div>
                <div className="flex items-center text-sm text-gray-600 dark:text-gray-400 font-mono bg-gray-50 dark:bg-gray-700/50 p-2 rounded">
                    <svg className="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
                    </svg>
                    <span className="font-medium mr-1">SHA:</span> {commit.sha}
                </div>
            </div>

            {/* 파일 목록과 비교 뷰의 비율 변경 (1:6) */}
            <div className="grid grid-cols-1 lg:grid-cols-7 gap-6">
                {/* 파일 목록 패널 - 더 좁게 조정 */}
                <div className="lg:col-span-1">
                    <h2 className="text-lg font-bold mb-4 flex items-center">
                        <svg className="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                        </svg>
                        <span className="truncate">Changed Files ({changedFiles.length})</span>
                    </h2>

                    {changedFiles.length === 0 ? (
                        <div className="bg-yellow-50 dark:bg-yellow-900/20 border-l-4 border-yellow-400 p-4 rounded">
                            <div className="flex">
                                <div className="flex-shrink-0">
                                    <svg className="h-5 w-5 text-yellow-400" viewBox="0 0 20 20" fill="currentColor">
                                        <path fillRule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
                                    </svg>
                                </div>
                                <div className="ml-3">
                                    <p className="text-sm text-yellow-700 dark:text-yellow-400">No files changed in this commit.</p>
                                </div>
                            </div>
                        </div>
                    ) : (
                        <div className="bg-white dark:bg-gray-800 shadow-md rounded-xl overflow-hidden">
                            <ul className="divide-y divide-gray-200 dark:divide-gray-700">
                                {changedFiles.map((file, index) => (
                                    <li
                                        key={index}
                                        className={`hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors cursor-pointer ${selectedFile === index ? 'bg-blue-50 dark:bg-blue-900/20' : ''}`}
                                        onClick={() => setSelectedFile(index)}
                                    >
                                        <div className="p-3 flex items-center">
                                            <div className={`w-2 h-2 rounded-full mr-2 flex-shrink-0 ${
                                                file.status === 'added' ? 'bg-green-500' :
                                                    file.status === 'modified' ? 'bg-yellow-500' :
                                                        file.status === 'removed' ? 'bg-red-500' : 'bg-gray-500'
                                            }`}></div>
                                            <span className="text-sm font-mono truncate">{file.fileName}</span>
                                        </div>
                                    </li>
                                ))}
                            </ul>
                        </div>
                    )}
                </div>

                {/* 파일 비교 패널 - 더 넓게 조정 */}
                <div className="lg:col-span-6">
                    {selectedFile !== null && changedFiles[selectedFile] && (
                        <div className="bg-white dark:bg-gray-800 shadow-md rounded-xl overflow-hidden">
                            <div className="border-b border-gray-200 dark:border-gray-700 px-6 py-4 flex items-center justify-between">
                                <h3 className="font-medium text-gray-900 dark:text-gray-100 flex items-center">
                                    <svg className="w-4 h-4 mr-2 text-gray-500 dark:text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                                    </svg>
                                    <span className="truncate max-w-sm">{changedFiles[selectedFile].fileName}</span>
                                </h3>
                                <div className="flex space-x-3">
                                    <span className={`px-2 py-1 rounded text-sm ${
                                        changedFiles[selectedFile].status === 'added' ? 'bg-green-100 text-green-800 dark:bg-green-900/20 dark:text-green-400' :
                                            changedFiles[selectedFile].status === 'modified' ? 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900/20 dark:text-yellow-400' :
                                                changedFiles[selectedFile].status === 'removed' ? 'bg-red-100 text-red-800 dark:bg-red-900/20 dark:text-red-400' :
                                                    'bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-300'
                                    }`}>
                                        {changedFiles[selectedFile].status}
                                    </span>
                                    <span className="px-2 py-1 rounded text-sm bg-blue-100 text-blue-800 dark:bg-blue-900/20 dark:text-blue-400">
                                        +{changedFiles[selectedFile].additions} -{changedFiles[selectedFile].deletions}
                                    </span>
                                    {/* 통합 뷰/분할 뷰 선택 버튼 제거 */}
                                </div>
                            </div>

                            {/* 파일 비교 뷰 - 항상 분할 뷰만 표시 */}
                            {diffLoading ? (
                                <div className="p-8 flex justify-center">
                                    <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500"></div>
                                </div>
                            ) : (
                                // 수정된 분할 뷰 - 공통 스크롤 사용
                                <div className="grid grid-cols-2 divide-x divide-gray-200 dark:divide-gray-700 overflow-auto max-h-[70vh]">
                                    <div>
                                        <div className="bg-gray-50 dark:bg-gray-700/50 px-4 py-2 text-xs font-semibold text-gray-700 dark:text-gray-300 sticky top-0">
                                            변경 전
                                        </div>
                                        <div>
                                            {fileDiff ? (
                                                <div className="font-mono text-sm">
                                                    {fileDiff.oldContent.split('\n').map((line, idx) => (
                                                        <pre
                                                            key={`old-${idx}`}
                                                            className={`px-4 py-1 ${
                                                                line.startsWith('-') ? 'bg-red-100 dark:bg-red-900/20 text-red-800 dark:text-red-400' : ''
                                                            }`}
                                                        >
                                                            {line}
                                                        </pre>
                                                    ))}
                                                </div>
                                            ) : (
                                                <div className="p-4 text-gray-500 dark:text-gray-400 italic">
                                                    No content available
                                                </div>
                                            )}
                                        </div>
                                    </div>
                                    <div>
                                        <div className="bg-gray-50 dark:bg-gray-700/50 px-4 py-2 text-xs font-semibold text-gray-700 dark:text-gray-300 sticky top-0">
                                            변경 후
                                        </div>
                                        <div>
                                            {fileDiff ? (
                                                <div className="font-mono text-sm">
                                                    {fileDiff.newContent.split('\n').map((line, idx) => (
                                                        <pre
                                                            key={`new-${idx}`}
                                                            className={`px-4 py-1 ${
                                                                line.startsWith('+') ? 'bg-green-100 dark:bg-green-900/20 text-green-800 dark:text-green-400' : ''
                                                            }`}
                                                        >
                                                            {line}
                                                        </pre>
                                                    ))}
                                                </div>
                                            ) : (
                                                <div className="p-4 text-gray-500 dark:text-gray-400 italic">
                                                    No content available
                                                </div>
                                            )}
                                        </div>
                                    </div>
                                </div>
                            )}
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}
